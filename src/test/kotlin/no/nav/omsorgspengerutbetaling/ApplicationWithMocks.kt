package no.nav.omsorgspengerutbetaling

import com.github.fppt.jedismock.RedisServer
import io.ktor.server.testing.withApplication
import no.nav.helse.dusseldorf.testsupport.asArguments
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.omsorgspengerutbetaling.mellomlagring.started
import no.nav.omsorgspengerutbetaling.wiremock.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ApplicationWithMocks {
    companion object {

        private val logger: Logger = LoggerFactory.getLogger(ApplicationWithMocks::class.java)

        @JvmStatic
        fun main(args: Array<String>) {

            val wireMockServer = WireMockBuilder()
                .withPort(8081)
                .withAzureSupport()
                .withNaisStsSupport()
                .withLoginServiceSupport()
                .omsorgspengesoknadApiConfig()
                .build()
                .stubK9DokumentHealth()
                .stubOmsorgspengerutbetalingsoknadMottakHealth()
                .stubOppslagHealth()
                .stubLeggSoknadTilProsessering("/v1/soknad")
                .stubLeggSoknadTilProsessering("/arbeidstaker/soknad")
                .stubK9Dokument()
                .stubK9OppslagSoker()

            val redisServer: RedisServer = RedisServer
                .newRedisServer(6379)
                .started()

            val testArgs = TestConfiguration.asMap(
                port = 8082,
                wireMockServer = wireMockServer,
                redisServer = redisServer
            ).asArguments()

            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    logger.info("Tearing down")
                    wireMockServer.stop()
                    redisServer.stop()
                    logger.info("Tear down complete")
                }
            })

            withApplication { no.nav.omsorgspengerutbetaling.main(testArgs) }
        }
    }
}
