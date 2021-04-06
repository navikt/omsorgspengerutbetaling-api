package no.nav.omsorgspengerutbetaling

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import no.finn.unleash.FakeUnleash
import no.finn.unleash.Unleash
import no.finn.unleash.event.UnleashReady
import no.finn.unleash.event.UnleashSubscriber
import no.finn.unleash.repository.FeatureToggleResponse
import no.finn.unleash.repository.ToggleCollection
import no.nav.helse.dusseldorf.ktor.auth.EnforceEqualsOrContains
import no.nav.helse.dusseldorf.ktor.auth.issuers
import no.nav.helse.dusseldorf.ktor.auth.withAdditionalClaimRules
import no.nav.helse.dusseldorf.ktor.core.getOptionalList
import no.nav.helse.dusseldorf.ktor.core.getRequiredList
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import no.nav.helse.dusseldorf.ktor.unleash.unleashConfig
import no.nav.omsorgspengerutbetaling.general.auth.ApiGatewayApiKey
import org.slf4j.LoggerFactory
import java.net.URI

@KtorExperimentalAPI
data class Configuration(val config : ApplicationConfig) {

    companion object {
        private val logger = LoggerFactory.getLogger(Configuration::class.java)
    }

    internal fun unleash(): Unleash {
        val unleash = config.unleashConfig(subscriber = object : UnleashSubscriber {
            override fun onReady(ready: UnleashReady) {
                logger.info("Unleash is ready")
            }

            override fun togglesFetched(response: FeatureToggleResponse) {
                when(response.status) {
                    FeatureToggleResponse.Status.CHANGED -> logger.info("Feature toggles changed: {}", response.toggleCollection.features)
                    FeatureToggleResponse.Status.UNAVAILABLE -> logger.warn("Feature toggles is not available. HTTP status code = {}", response.httpStatusCode)
                    else -> {}
                }
            }

            override fun togglesBackedUp(toggleCollection: ToggleCollection) {
                logger.info("Backup stored.")
            }
        })
        if (unleash is FakeUnleash) unleash.enableAll()
        return unleash
    }

    private val loginServiceClaimRules = setOf(
        EnforceEqualsOrContains("acr", "Level4")
    )

    internal fun issuers() = config.issuers().withAdditionalClaimRules(mapOf(
        "login-service-v1" to loginServiceClaimRules,
        "login-service-v2" to loginServiceClaimRules
    ))

    internal fun getCookieName() : String {
        return config.getRequiredString("nav.authorization.cookie_name", secret = false)
    }

    internal fun getWhitelistedCorsAddreses() : List<URI> {
        return config.getOptionalList(
            key = "nav.cors.addresses",
            builder = { value ->
                URI.create(value)
            },
            secret = false
        )
    }

    internal fun getK9OppslagUrl() = URI(config.getRequiredString("nav.gateways.k9_oppslag_url", secret = false))

    internal fun getK9DokumentUrl() = URI(config.getRequiredString("nav.gateways.k9_dokument_url", secret = false))

    internal fun getOmsorgpengesoknadMottakBaseUrl() = URI(config.getRequiredString("nav.gateways.omsorgpengesoknad_mottak_base_url", secret = false))

    internal fun getApiGatewayApiKey() : ApiGatewayApiKey {
        val apiKey = config.getRequiredString(key = "nav.authorization.api_gateway.api_key", secret = true)
        return ApiGatewayApiKey(value = apiKey)
    }

    private fun getScopesFor(operation: String) = config.getRequiredList("nav.auth.scopes.$operation", secret = false, builder = { it }).toSet()
    internal fun getSendSoknadTilProsesseringScopes() = getScopesFor("sende-soknad-til-prosessering")
    internal fun getRedisPort() = config.getRequiredString("nav.redis.port", secret = false).toInt()
    internal fun getRedisHost() = config.getRequiredString("nav.redis.host", secret = false)

    internal fun getStoragePassphrase() : String {
        return config.getRequiredString("nav.storage.passphrase", secret = true)
    }
}
