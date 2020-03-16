package no.nav.omsorgspengerutbetaling.barn

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.omsorgspengerutbetaling.general.auth.IdTokenProvider
import no.nav.omsorgspengerutbetaling.general.getCallId

@KtorExperimentalLocationsAPI
fun Route.barnApis(
    barnService: BarnService,
    idTokenProvider: IdTokenProvider
) {

    @Location("/barn")
    class getBarn

    get { _: getBarn ->
        call.respond(
            BarnResponse(
                barnService.hentNaaverendeBarn(
                    idToken = idTokenProvider.getIdToken(call),
                    callId = call.getCallId()
                )
            )
        )
    }
}
