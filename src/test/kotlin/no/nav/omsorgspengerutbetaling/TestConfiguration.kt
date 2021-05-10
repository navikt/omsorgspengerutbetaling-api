package no.nav.omsorgspengerutbetaling

import com.github.fppt.jedismock.RedisServer
import com.github.kittinunf.fuel.httpGet
import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.helse.dusseldorf.testsupport.jws.ClientCredentials
import no.nav.helse.dusseldorf.testsupport.jws.LoginService
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2WellKnownUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getLoginServiceV1WellKnownUrl
import no.nav.omsorgspengerutbetaling.wiremock.getK9DokumentUrl
import no.nav.omsorgspengerutbetaling.wiremock.getK9OppslagUrl
import no.nav.omsorgspengerutbetaling.wiremock.getOmsorgpengerutbetalingsoknadMottakUrl
import org.json.JSONObject

object TestConfiguration {

    fun asMap(
        wireMockServer: WireMockServer? = null,
        port : Int = 8080,
        k9OppslagUrl: String? = wireMockServer?.getK9OppslagUrl(),
        omsorgpengerutbetalingsoknadMottakUrl : String? = wireMockServer?.getOmsorgpengerutbetalingsoknadMottakUrl(),
        k9DokumentUrl : String? = wireMockServer?.getK9DokumentUrl(),
        corsAdresses : String = "http://localhost:8080",
        redisServer: RedisServer,
        cluster: String = "test",
        unleashAPI: String = "http://localhost:8080/unleash/api/"
    ) : Map<String, String> {

        val map = mutableMapOf(
            Pair("ktor.deployment.port","$port"),
            Pair("nav.authorization.cookie_name", "localhost-idtoken"),
            Pair("nav.gateways.k9_oppslag_url","$k9OppslagUrl"),
            Pair("nav.gateways.omsorgpengesoknad_mottak_base_url", "$omsorgpengerutbetalingsoknadMottakUrl"),
            Pair("nav.gateways.k9_dokument_url", "$k9DokumentUrl"),
            Pair("nav.cors.addresses", corsAdresses),
            Pair("nav.authorization.api_gateway.api_key", "verysecret")
        )

        if (wireMockServer != null) {
            // Clients
            map["nav.auth.clients.0.alias"] = "azure-v2"
            map["nav.auth.clients.0.client_id"] = "omsorgspengesoknad-api"
            map["nav.auth.clients.0.private_key_jwk"] = ClientCredentials.ClientC.privateKeyJwk
            map["nav.auth.clients.0.discovery_endpoint"] = wireMockServer.getAzureV2WellKnownUrl()
            map["nav.auth.scopes.sende-soknad-til-prosessering"] = "omsorgspengerutbetalingsoknad-mottak/.default"

            // Issuers
            map["nav.auth.issuers.0.alias"] = "login-service-v1"
            map["nav.auth.issuers.0.discovery_endpoint"] = wireMockServer.getLoginServiceV1WellKnownUrl()
            map["nav.auth.issuers.1.alias"] = "login-service-v2"
            map["nav.auth.issuers.1.discovery_endpoint"] = wireMockServer.getLoginServiceV1WellKnownUrl()
            map["nav.auth.issuers.1.audience"] = LoginService.V1_0.getAudience()
        }

        map["nav.redis.host"] = "localhost"
        map["nav.redis.port"] = "${redisServer.bindPort}"
        map["nav.storage.passphrase"] = "verySecret"

        return map.toMap()
    }
}
