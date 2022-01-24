package no.nav.omsorgspengerutbetaling.soknad

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.omsorgspengerutbetaling.felles.SØKNAD_URL
import no.nav.omsorgspengerutbetaling.felles.VALIDER_URL
import no.nav.omsorgspengerutbetaling.felles.formaterStatuslogging
import no.nav.omsorgspengerutbetaling.general.getCallId
import no.nav.omsorgspengerutbetaling.general.getMetadata
import no.nav.omsorgspengerutbetaling.k9format.tilK9Format
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soker.SøkerService
import no.nav.omsorgspengerutbetaling.soker.validate
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
                idToken = idTokenProvider.getIdToken(call),
                metadata = call.getMetadata()
            )

            call.respond(HttpStatusCode.Accepted)
        }

        post(VALIDER_URL) {
            val søknad = call.receive<Søknad>()
            logger.info("Validerer søknad...")

            val søker: Søker = søkerService.getSoker(idToken = idTokenProvider.getIdToken(call), callId = call.getCallId())
            søker.validate()

            val k9FormatSøknad = søknad.tilK9Format(søker)
            k9FormatSøknad.valider()
            søknad.valider()
            logger.info("Validering OK.")
            call.respond(HttpStatusCode.Accepted)
        }
    }
}