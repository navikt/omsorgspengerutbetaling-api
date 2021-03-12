package no.nav.omsorgspengerutbetaling.barn

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.*
import no.nav.omsorgspengerutbetaling.general.auth.IdTokenProvider
import no.nav.omsorgspengerutbetaling.general.getCallId

const val BARN_URL = "/barn"

fun Route.barnApis(
    barnService: BarnService,
    idTokenProvider: IdTokenProvider
) {
    get(BARN_URL) {
        call.respond(
            BarnResponse(
                barnService.hentNåværendeBarn(
                    idToken = idTokenProvider.getIdToken(call),
                    callId = call.getCallId()
                )
            )
        )
    }
}