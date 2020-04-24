package no.nav.omsorgspengerutbetaling.arbeidstakerutbetaling

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.omsorgspengerutbetaling.general.auth.IdTokenProvider
import no.nav.omsorgspengerutbetaling.general.getCallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

@KtorExperimentalLocationsAPI
internal fun Route.arbeidstakerutbetalingsøknadApis(
    søknadService: ArbeidstakerutbetalingSøknadService,
    idTokenProvider: IdTokenProvider
) {

    @Location("/arbeidstaker/soknad")
    class sendSoknadUtbetalingArbeidstaker

    post { _ : sendSoknadUtbetalingArbeidstaker ->
        logger.trace("Mottatt ny søknad om arbeidstakerutbetaling. Mapper søknad.")
        val søknad = call.receive<Arbeidstakerutbetalingsøknad>()

        logger.trace("Søknad mappet. Validerer")

        søknad.valider()
        logger.trace("Validering OK. Registrerer søknad.")

        søknadService.registrer(
            søknad = søknad,
            callId = call.getCallId(),
            idToken = idTokenProvider.getIdToken(call)
        )

        logger.trace("Søknad registrert.")
        call.respond(HttpStatusCode.Accepted)
    }

    @Location("/valider/soknad/arbeidstaker")
    class validerSoknad

    post { _ : validerSoknad ->
        val søknad = call.receive<Arbeidstakerutbetalingsøknad>()
        logger.info("Validerer søknad...")
        søknad.valider()
        logger.trace("Validering OK.")
        call.respond(HttpStatusCode.Accepted)
    }
}
