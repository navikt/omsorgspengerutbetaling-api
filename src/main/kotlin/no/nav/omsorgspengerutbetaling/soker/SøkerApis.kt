package no.nav.omsorgspengerutbetaling.soker

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspengerutbetaling.felles.SØKER_URL
import no.nav.omsorgspengerutbetaling.general.auth.IdTokenProvider
import no.nav.omsorgspengerutbetaling.general.getCallId
import no.nav.omsorgspengerutbetaling.general.oppslag.TilgangNektetException
import no.nav.omsorgspengerutbetaling.general.oppslag.respondTilgangNektetProblemDetail
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("no.nav.omsorgspengerutbetaling.soker.søkerApis")

fun Route.søkerApis(
    søkerService: SøkerService,
    idTokenProvider: IdTokenProvider
) {
    get(SØKER_URL) {
        try {
            call.respond(
                søkerService.getSoker(
                    idTokenProvider.getIdToken(call), call.getCallId()
                )
            )
        } catch (e: Exception) {
            when(e){
                is TilgangNektetException -> call.respondTilgangNektetProblemDetail(logger, e)
                else -> throw e
            }
        }
    }
}