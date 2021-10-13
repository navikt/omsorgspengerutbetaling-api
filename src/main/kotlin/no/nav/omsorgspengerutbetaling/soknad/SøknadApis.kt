package no.nav.omsorgspengerutbetaling.soknad

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspengerutbetaling.felles.SØKNAD_URL
import no.nav.omsorgspengerutbetaling.felles.VALIDER_URL
import no.nav.omsorgspengerutbetaling.felles.formaterStatuslogging
import no.nav.omsorgspengerutbetaling.general.auth.IdTokenProvider
import no.nav.omsorgspengerutbetaling.general.getCallId
import no.nav.omsorgspengerutbetaling.k9format.tilKOmsorgspengerUtbetalingSøknad
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soker.SøkerService
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
            val søknad = call.receive<Søknad>()
            logger.info(formaterStatuslogging(søknad.søknadId.id, "mottatt"))

            søknadService.registrer(
                søknad = søknad,
                callId = call.getCallId(),
                idToken = idTokenProvider.getIdToken(call)
            )

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