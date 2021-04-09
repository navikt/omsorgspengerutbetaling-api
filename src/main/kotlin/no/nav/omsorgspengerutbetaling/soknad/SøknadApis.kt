package no.nav.omsorgspengerutbetaling.soknad

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.helse.dusseldorf.ktor.unleash.UnleashService
import no.nav.omsorgspengerutbetaling.FeatureFlag
import no.nav.omsorgspengerutbetaling.general.auth.IdTokenProvider
import no.nav.omsorgspengerutbetaling.general.getCallId
import no.nav.omsorgspengerutbetaling.k9format.tilKOmsorgspengerUtbetalingSøknad
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soker.SøkerService
import no.nav.omsorgspengerutbetaling.soker.validate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

@KtorExperimentalLocationsAPI
internal fun Route.søknadApis(
    søknadService: SøknadService,
    søkerService: SøkerService,
    idTokenProvider: IdTokenProvider,
    unleashService: UnleashService
) {

    @Location("/soknad")
    class sendSoknad

    post { _: sendSoknad ->
        logger.trace("Mottatt ny søknad. Mapper søknad.")
        val søknad = call.receive<Søknad>()
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)
        val idToken = idTokenProvider.getIdToken(call)
        val callId = call.getCallId()

        logger.trace("Registrerer søknad. Henter søker")

        val søker: Søker = søkerService.getSoker(idToken = idToken, callId = callId)

        logger.trace("Søker hentet. Validerer søker.")
        søker.validate()
        logger.trace("Søker Validert.")

        logger.trace("Søknad mappet. Validerer")
        logger.info("Mapper om søknad til k9format.")
        val k9FormatSøknad = søknad.tilKOmsorgspengerUtbetalingSøknad(
            mottatt = mottatt,
            søker = søker
        )

        if (unleashService.isEnabled(FeatureFlag.OMP_UT_SNF_SOKNAD_VALIDERING, true)) {
            søknad.valider(k9FormatSøknad)
            logger.trace("Validering OK. Registrerer søknad.")
        } else logger.info("Validering av søknad er deaktivert.")

        søknadService.registrer(
            søknad = søknad,
            k9FormatSøknad = k9FormatSøknad,
            mottatt = mottatt,
            søker = søker,
            callId = callId,
            idToken = idToken
        )

        logger.trace("Søknad registrert.")
        call.respond(HttpStatusCode.Accepted)
    }

    @Location("/soknad/valider")
    class validerSoknad

    post { _: validerSoknad ->
        val søknad = call.receive<Søknad>()
        logger.info("Validerer søknad...")
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)
        val idToken = idTokenProvider.getIdToken(call)
        val callId = call.getCallId()

        val søker: Søker = søkerService.getSoker(idToken = idToken, callId = callId)

        val k9FormatSøknad = søknad.tilKOmsorgspengerUtbetalingSøknad(mottatt, søker)
        søknad.valider(k9FormatSøknad)
        logger.trace("Validering OK.")
        call.respond(HttpStatusCode.Accepted)
    }
}
