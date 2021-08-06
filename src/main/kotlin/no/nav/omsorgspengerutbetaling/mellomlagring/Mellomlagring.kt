package no.nav.omsorgspengerutbetaling.mellomlagring

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspengerutbetaling.felles.MELLOMLAGRING_URL
import no.nav.omsorgspengerutbetaling.general.auth.IdTokenProvider

fun Route.mellomlagringApis(
    mellomlagringService: MellomlagringService,
    idTokenProvider: IdTokenProvider
) {

    route(MELLOMLAGRING_URL) {
        post {
            val midlertidigSøknad = call.receive<String>()
            val idToken = idTokenProvider.getIdToken(call)
            mellomlagringService.setMellomlagring(idToken.getSubject()!!, midlertidigSøknad)
            call.respond(HttpStatusCode.NoContent)
        }

        put {
            val midlertidigSøknad = call.receive<String>()
            val idToken = idTokenProvider.getIdToken(call)
            mellomlagringService.updateMellomlagring(idToken.getSubject()!!, midlertidigSøknad)
            call.respond(HttpStatusCode.NoContent)
        }

        get {
            val idToken = idTokenProvider.getIdToken(call)
            val mellomlagring = mellomlagringService.getMellomlagring(idToken.getSubject()!!)
            if (mellomlagring != null) {
                call.respondText(
                    contentType = ContentType.Application.Json,
                    text = mellomlagring,
                    status = HttpStatusCode.OK
                )
            } else {
                call.respondText(
                    contentType = ContentType.Application.Json,
                    text = "{}",
                    status = HttpStatusCode.OK
                )
            }
        }

        delete {
            val idToken = idTokenProvider.getIdToken(call)
            mellomlagringService.deleteMellomlagring(idToken.getSubject()!!)
            call.respond(HttpStatusCode.Accepted)
        }
    }
}