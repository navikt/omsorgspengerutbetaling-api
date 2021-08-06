package no.nav.omsorgspengerutbetaling.soknad

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspengerutbetaling.felles.SØKNAD_URL
import no.nav.omsorgspengerutbetaling.felles.VALIDER_URL
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

internal fun Route.søknadApis(
    søknadService: SøknadService,
    søkerService: SøkerService,
    idTokenProvider: IdTokenProvider
) {

    route(SØKNAD_URL) {
        post {
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

            søknad.valider(k9FormatSøknad)
            logger.trace("Validering OK. Registrerer søknad.")

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

        post(VALIDER_URL) {
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
}