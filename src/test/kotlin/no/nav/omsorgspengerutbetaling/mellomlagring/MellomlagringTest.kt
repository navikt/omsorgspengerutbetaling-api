package no.nav.omsorgspengerutbetaling.mellomlagring

import com.github.fppt.jedismock.RedisServer
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.omsorgspengerutbetaling.redis.RedisConfig
import no.nav.omsorgspengerutbetaling.redis.RedisStore
import no.nav.omsorgspengerutbetaling.wiremock.*
import no.nav.omsorgspengerutbetaling.wiremock.omsorgspengesoknadApiConfig
import no.nav.omsorgspengerutbetaling.wiremock.stubK9DokumentHealth
import no.nav.omsorgspengerutbetaling.wiremock.stubOmsorgspengerutbetalingsoknadMottakHealth
import no.nav.omsorgspengerutbetaling.wiremock.stubOppslagHealth
import org.junit.AfterClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@KtorExperimentalAPI
class MellomlagringTest {
    private companion object {
        val redisServer: RedisServer = RedisServer
            .newRedisServer(6379)
            .started()

        val redisClient = RedisConfig.redisClient(
            redisHost = redisServer.host,
            redisPort = redisServer.bindPort
        )

        val redisStore = RedisStore(
            redisClient = redisClient
        )

        val mellomlagringService = MellomlagringService(
            redisStore,
            "VerySecretPass"
        )

        @AfterClass
        @JvmStatic
        fun teardown() {
            redisClient.shutdown()
            redisServer.stop()
        }
    }

    @Test
    internal fun `mellomlagre verdier`() {
        mellomlagringService.setMellomlagring("test", "test")

        val mellomlagring = mellomlagringService.getMellomlagring("test")

        assertEquals("test", mellomlagring)
    }

    @Test
    internal fun `verdier skal være krypterte`() {

        mellomlagringService.setMellomlagring("test", "test")

        val mellomlagring = mellomlagringService.getMellomlagring("test")
        assertNotNull(redisStore.get("mellomlagring_test"))
        assertNotEquals(mellomlagring, redisStore.get("test"))
    }

}
