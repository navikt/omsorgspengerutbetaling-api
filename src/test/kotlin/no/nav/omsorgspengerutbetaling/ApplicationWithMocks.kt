package no.nav.omsorgspengerutbetaling

import io.ktor.server.testing.withApplication
import no.nav.helse.dusseldorf.testsupport.asArguments
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
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
                .stubOmsorgsoknadMottakHealth()
                .stubOppslagHealth()
                .stubLeggSoknadTilProsessering()
                .stubK9Dokument()
                .stubK9OppslagSoker()
                .stubK9OppslagBarn()

            val testArgs = TestConfiguration.asMap(
                port = 8082,
                wireMockServer = wireMockServer
            ).asArguments()

            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    logger.info("Tearing down")
                    wireMockServer.stop()
                    logger.info("Tear down complete")
                }
            })

            withApplication { no.nav.omsorgspengerutbetaling.main(testArgs) }
        }
    }
}