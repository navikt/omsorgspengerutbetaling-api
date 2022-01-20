package no.nav.omsorgspengerutbetaling.barn

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.omsorgspengerutbetaling.felles.BARN_URL
import no.nav.omsorgspengerutbetaling.general.getCallId
import no.nav.omsorgspengerutbetaling.general.oppslag.TilgangNektetException
import no.nav.omsorgspengerutbetaling.general.oppslag.respondTilgangNektetProblemDetail
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.omsorgsdagermeldingapi.barn.barnApis")

fun Route.barnApis(
    barnService: BarnService,
    idTokenProvider: IdTokenProvider
) {
    get(BARN_URL) {
        try {
            call.respond(
                BarnResponse(barnService.hentNåværendeBarn(idTokenProvider.getIdToken(call), call.getCallId()))
            )
        } catch (e: Exception) {
            when(e){
                is TilgangNektetException -> call.respondTilgangNektetProblemDetail(logger, e)
                else -> throw e
            }
        }
    }
}
