package no.nav.omsorgspengerutbetaling.barn

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.oppslag.K9OppslagGateway
import no.nav.omsorgspengerutbetaling.general.oppslag.throwable
import no.nav.omsorgspengerutbetaling.k9SelvbetjeningOppslagKonfigurert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration
import java.time.LocalDate

class BarnGateway(
    baseUrl: URI
) : K9OppslagGateway(baseUrl) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger("nav.BarnGateway")
        private const val HENTE_BARN_OPERATION = "hente-barn"
        private val objectMapper = k9SelvbetjeningOppslagKonfigurert()

        private val attributter = Pair(
            "a", listOf(
                "barn[].aktør_id",
                "barn[].fornavn",
                "barn[].mellomnavn",
                "barn[].etternavn",
                "barn[].fødselsdato",
                "barn[].identitetsnummer"
            )
        )
    }

    suspend fun hentBarn(
        idToken: IdToken,
        callId: CallId
    ): List<BarnOppslagDTO> {
        val barnUrl = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("meg"),
            queryParameters = mapOf(
                attributter
            )
        ).toString()

        val httpRequest = generateHttpRequest(idToken, barnUrl, callId)

        val oppslagRespons = Retry.retry(
            operation = HENTE_BARN_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "omsorgsdager-melding-api",
                operation = HENTE_BARN_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<BarnOppslagResponse>(success) },
                { error -> throw error.throwable(request, logger, "Feil ved henting av informasjon om søkers barn") }
            )
        }
        return oppslagRespons.barn
    }

    private data class BarnOppslagResponse(val barn: List<BarnOppslagDTO>)

    data class BarnOppslagDTO(
        val fødselsdato: LocalDate,
        val fornavn: String,
        val mellomnavn: String? = null,
        val etternavn: String,
        val aktør_id: String,
        val identitetsnummer: String? = null
    )

}
