package no.nav.omsorgspengerutbetaling.general.oppslag

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.HttpHeaders
import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.auth.IdToken
import java.net.URI

abstract class K9OppslagGateway(
    protected val baseUrl: URI
) {

    protected fun generateHttpRequest(
        idToken: IdToken,
        url: String,
        callId: CallId
    ): Request {
        return url
            .httpGet()
            .header(
                HttpHeaders.Authorization to "Bearer ${idToken.value}",
                HttpHeaders.Accept to "application/json",
                HttpHeaders.XCorrelationId to callId.value
            )
    }
}
