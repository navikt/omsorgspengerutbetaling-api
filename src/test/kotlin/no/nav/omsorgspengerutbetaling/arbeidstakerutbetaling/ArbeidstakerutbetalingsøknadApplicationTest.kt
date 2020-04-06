package no.nav.omsorgspengerutbetaling.arbeidstakerutbetaling

import com.github.fppt.jedismock.RedisServer
import com.github.tomakehurst.wiremock.http.Cookie
import com.typesafe.config.ConfigFactory
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.*
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.omsorgspengerutbetaling.*
import no.nav.omsorgspengerutbetaling.mellomlagring.started
import no.nav.omsorgspengerutbetaling.felles.*
import no.nav.omsorgspengerutbetaling.wiremock.*
import org.junit.AfterClass
import org.junit.BeforeClass
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals

private const val fnr = "290990123456"
private const val ikkeMyndigFnr = "12125012345"

// Se https://github.com/navikt/dusseldorf-ktor#f%C3%B8dselsnummer
private val gyldigFodselsnummerA = "02119970078"
private val ikkeMyndigDato = "2050-12-12"

@KtorExperimentalAPI
class ArbeidstakerutbetalingsøknadApplicationTest {

    private companion object {

        private val logger: Logger = LoggerFactory.getLogger(ArbeidstakerutbetalingsøknadApplicationTest::class.java)

        val wireMockServer = WireMockBuilder()
            .withAzureSupport()
            .withNaisStsSupport()
            .withLoginServiceSupport()
            .omsorgspengesoknadApiConfig()
            .build()
            .stubK9DokumentHealth()
            .stubOmsorgspengerutbetalingsoknadMottakHealth()
            .stubOppslagHealth()
            .stubLeggSoknadTilProsessering("/v1/arbeidstaker/soknad")
            .stubK9OppslagSoker()
            .stubK9Dokument()

        val redisServer: RedisServer = RedisServer
            .newRedisServer(6379)
            .started()

        fun getConfig(): ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap(
                    wireMockServer = wireMockServer,
                    redisServer = redisServer
                )
            )
            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }


        val engine = TestApplicationEngine(createTestEnvironment {
            config = getConfig()
        })


        @BeforeClass
        @JvmStatic
        fun buildUp() {
            engine.start(wait = true)
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            logger.info("Tearing down")
            wireMockServer.stop()
            redisServer.stop()
            logger.info("Tear down complete")
        }
    }

    @Test
    fun `Sende soknad`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/arbeidstaker/soknad",
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = ArbeidstakerutbetalingSøknadUtils.defaultSøknad.somJson()
        )
    }

    @Test
    fun `Sende soknad som raw json`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/arbeidstaker/soknad",
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = """
                {
                    "språk": "nb",
                    "bosteder": [{
                        "fraOgMed": "2019-12-12",
                        "tilOgMed": "2019-12-22",
                        "landkode": "GB",
                        "landnavn": "Great Britain",
                        "erEØSLand": true
                    }],
                    "opphold": [{
                        "fraOgMed": "2019-12-12",
                        "tilOgMed": "2019-12-22",
                        "landkode": "GB",
                        "landnavn": "Great Britain",
                        "erEØSLand": true
                    }],
                    "jobbHosNåværendeArbeidsgiver": {
                        "merEnn4Uker": true,
                        "begrunnelse": "ANNET_ARBEIDSFORHOLD"
                    },
                    "arbeidsgivere": {
                      "organisasjoner": [
                        {
                          "navn": "Arbeidsgiver 1",
                          "organisasjonsnummer": "917755736",
                          "skalJobbeProsent": 100.0,
                          "skalJobbe": "ja",
                          "jobberNormaltTimer": 37.5,
                          "vetIkkeEkstrainfo": null
                        },
                        {
                          "navn": "Arbeidsgiver 2",
                          "organisasjonsnummer": "917755736",
                          "skalJobbeProsent": 50.0,
                          "skalJobbe": "redusert",
                          "jobberNormaltTimer": 37.5,
                          "vetIkkeEkstrainfo": null
                        },
                        {
                          "navn": "Arbeidsgiver 3",
                          "organisasjonsnummer": "917755736",
                          "skalJobbeProsent": 0.0,
                          "skalJobbe": "vet_ikke",
                          "vetIkkeEkstrainfo": "Usikker på om jeg skal jobbe.",
                          "jobberNormaltTimer": 37.5
                        },
                        {
                          "navn": null,
                          "organisasjonsnummer": "917755736",
                          "skalJobbeProsent": 0.0,
                          "skalJobbe": "nei",
                          "jobberNormaltTimer": 37.5,
                          "vetIkkeEkstrainfo": null
                        }
                      ]
                    },
                    "spørsmål": [{
                        "spørsmål": "Et spørsmål",
                        "svar": false
                    }],
                    "bekreftelser": {
                        "harBekreftetOpplysninger": true,
                        "harForståttRettigheterOgPlikter": true
                    },
                    "utbetalingsperioder": [{
                        "fraOgMed": "2020-01-01",
                        "tilOgMed": "2020-01-11",
                        "lengde": null
                    }, {
                        "fraOgMed": "2020-01-21",
                        "tilOgMed": "2020-01-21",
                        "lengde": "PT5H30M"
                    }, {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-05",
                        "lengde": null
                    }],
                    "andreUtbetalinger": ["dagpenger", "sykepenger"]
                }
            """.trimIndent()
        )
    }

    @Test
    fun `Sende søknad ikke innlogget`() {
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/arbeidstaker/soknad",
            expectedCode = HttpStatusCode.Unauthorized,
            expectedResponse = null,
            requestEntity = ArbeidstakerutbetalingSøknadUtils.defaultSøknad.somJson(),
            leggTilCookie = false
        )
    }

    @Test
    fun `Sende soknad ikke myndig`() {
        val cookie = getAuthCookie(ikkeMyndigFnr)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/arbeidstaker/soknad",
            expectedResponse = """
                {
                    "type": "/problem-details/unauthorized",
                    "title": "unauthorized",
                    "status": 403,
                    "detail": "Søkeren er ikke myndig og kan ikke sende inn søknaden.",
                    "instance": "about:blank"
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.Forbidden,
            cookie = cookie,
            requestEntity = ArbeidstakerutbetalingSøknadUtils.defaultSøknad.somJson()
        )
    }

    @Test
    fun `Sende soknad med ugylidge parametre gir feil`() {
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/arbeidstaker/soknad",
            expectedCode = HttpStatusCode.BadRequest,
            requestEntity = ArbeidstakerutbetalingSøknadUtils.defaultSøknad.copy(
                bekreftelser = Bekreftelser(
                    harForståttRettigheterOgPlikter = JaNei.Nei,
                    harBekreftetOpplysninger = JaNei.Nei
                ),
                spørsmål = listOf(),
                utbetalingsperioder = listOf()
            ).somJson(),
            expectedResponse = """
            {
                "type": "/problem-details/invalid-request-parameters",
                "title": "invalid-request-parameters",
                "status": 400,
                "detail": "Requesten inneholder ugyldige paramtere.",
                "instance": "about:blank",
                "invalid_parameters": [{
                    "type": "entity",
                    "name": "utbetalingsperioder",
                    "reason": "Må settes minst en utbetalingsperiode.",
                    "invalid_value": []
                }, {
                    "type": "entity",
                    "name": "bekreftlser.harBekreftetOpplysninger",
                    "reason": "Må besvars Ja.",
                    "invalid_value": false
                }, {
                    "type": "entity",
                    "name": "bekreftelser.harForståttRettigheterOgPlikter",
                    "reason": "Må besvars Ja.",
                    "invalid_value": false
                }]
            }
            """.trimIndent()
        )
    }

    @Test
    fun `Sende søknad ugyldig fødselsnummer på fosterbarn, gir feilmelding`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/arbeidstaker/soknad",
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "fosterbarn[1].fødselsnummer",
                      "reason": "Ikke gyldig fødselsnummer.",
                      "invalid_value": "ugyldig fødselsnummer"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = ArbeidstakerutbetalingSøknadUtils.defaultSøknad.copy(
                fosterbarn = listOf(
                    FosterBarn(
                        fødselsnummer = "02119970078"
                    ),
                    FosterBarn(
                        fødselsnummer = "ugyldig fødselsnummer"
                    )
                )
            ).somJson()
        )
    }

    @Test
    fun `Sende søknad med ugyldig andreUtbetalinger`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/arbeidstaker/soknad",
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "andreUtbetalinger[1]",
                      "reason": "Ugyldig verdig for andre utbetaling. Kun 'dagpenger' og 'sykepenger' er tillatt.",
                      "invalid_value": "koronapenger"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = """
                 {
                    "språk": "nb",
                    "bosteder": [{
                        "fraOgMed": "2019-12-12",
                        "tilOgMed": "2019-12-22",
                        "landkode": "GB",
                        "landnavn": "Great Britain",
                        "erEØSLand": true
                    }],
                    "opphold": [{
                        "fraOgMed": "2019-12-12",
                        "tilOgMed": "2019-12-22",
                        "landkode": "GB",
                        "landnavn": "Great Britain",
                        "erEØSLand": true
                    }],
                    "jobbHosNåværendeArbeidsgiver": {
                        "merEnn4Uker": true,
                        "begrunnelse": "ANNET_ARBEIDSFORHOLD"
                    },
                    "arbeidsgivere": {
                      "organisasjoner": [
                        {
                          "navn": "Arbeidsgiver 1",
                          "organisasjonsnummer": "917755736",
                          "skalJobbeProsent": 100.0,
                          "skalJobbe": "ja",
                          "jobberNormaltTimer": 37.5,
                          "vetIkkeEkstrainfo": null
                        },
                        {
                          "navn": "Arbeidsgiver 2",
                          "organisasjonsnummer": "917755736",
                          "skalJobbeProsent": 50.0,
                          "skalJobbe": "redusert",
                          "jobberNormaltTimer": 37.5,
                          "vetIkkeEkstrainfo": null
                        },
                        {
                          "navn": "Arbeidsgiver 3",
                          "organisasjonsnummer": "917755736",
                          "skalJobbeProsent": 0.0,
                          "skalJobbe": "vet_ikke",
                          "vetIkkeEkstrainfo": "Usikker på om jeg skal jobbe.",
                          "jobberNormaltTimer": 37.5
                        },
                        {
                          "navn": null,
                          "organisasjonsnummer": "917755736",
                          "skalJobbeProsent": 0.0,
                          "skalJobbe": "nei",
                          "jobberNormaltTimer": 37.5,
                          "vetIkkeEkstrainfo": null
                        }
                      ]
                    },
                    "spørsmål": [{
                        "spørsmål": "Et spørsmål",
                        "svar": false
                    }],
                    "bekreftelser": {
                        "harBekreftetOpplysninger": true,
                        "harForståttRettigheterOgPlikter": true
                    },
                    "utbetalingsperioder": [{
                        "fraOgMed": "2020-01-01",
                        "tilOgMed": "2020-01-11",
                        "lengde": null
                    }, {
                        "fraOgMed": "2020-01-21",
                        "tilOgMed": "2020-01-21",
                        "lengde": "PT5H30M"
                    }, {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-05",
                        "lengde": null
                    }],
                    "andreUtbetalinger": ["dagpenger", "koronapenger"]
                }
                """.trimIndent()
        )
    }

    @Test
    fun `Sende ugyldig søknad, der begrunnelse på jobbHosNåværendeArbeidsgiver ikke er satt når arbeidsforholder har vart mer enn 4 uker`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/arbeidstaker/soknad",
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "jobbHosNåværendeArbeidsgiver.begrunnelse",
                      "reason": "Begrunnelse kan ikke være null, dersom merEnn4Uker er satt til true.",
                      "invalid_value": null
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = """
                 {
                    "språk": "nb",
                    "bosteder": [{
                        "fraOgMed": "2019-12-12",
                        "tilOgMed": "2019-12-22",
                        "landkode": "GB",
                        "landnavn": "Great Britain",
                        "erEØSLand": true
                    }],
                    "opphold": [{
                        "fraOgMed": "2019-12-12",
                        "tilOgMed": "2019-12-22",
                        "landkode": "GB",
                        "landnavn": "Great Britain",
                        "erEØSLand": true
                    }],
                    "jobbHosNåværendeArbeidsgiver": {
                        "merEnn4Uker": true
                    },
                    "arbeidsgivere": {
                      "organisasjoner": [
                        {
                          "navn": "Arbeidsgiver 1",
                          "organisasjonsnummer": "917755736",
                          "skalJobbeProsent": 100.0,
                          "skalJobbe": "ja",
                          "jobberNormaltTimer": 37.5,
                          "vetIkkeEkstrainfo": null
                        },
                        {
                          "navn": "Arbeidsgiver 2",
                          "organisasjonsnummer": "917755736",
                          "skalJobbeProsent": 50.0,
                          "skalJobbe": "redusert",
                          "jobberNormaltTimer": 37.5,
                          "vetIkkeEkstrainfo": null
                        },
                        {
                          "navn": "Arbeidsgiver 3",
                          "organisasjonsnummer": "917755736",
                          "skalJobbeProsent": 0.0,
                          "skalJobbe": "vet_ikke",
                          "vetIkkeEkstrainfo": "Usikker på om jeg skal jobbe.",
                          "jobberNormaltTimer": 37.5
                        },
                        {
                          "navn": null,
                          "organisasjonsnummer": "917755736",
                          "skalJobbeProsent": 0.0,
                          "skalJobbe": "nei",
                          "jobberNormaltTimer": 37.5,
                          "vetIkkeEkstrainfo": null
                        }
                      ]
                    },
                    "spørsmål": [{
                        "spørsmål": "Et spørsmål",
                        "svar": false
                    }],
                    "bekreftelser": {
                        "harBekreftetOpplysninger": true,
                        "harForståttRettigheterOgPlikter": true
                    },
                    "utbetalingsperioder": [{
                        "fraOgMed": "2020-01-01",
                        "tilOgMed": "2020-01-11",
                        "lengde": null
                    }, {
                        "fraOgMed": "2020-01-21",
                        "tilOgMed": "2020-01-21",
                        "lengde": "PT5H30M"
                    }, {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-05",
                        "lengde": null
                    }],
                    "andreUtbetalinger": ["dagpenger", "sykepenger"]
                }
                """.trimIndent()
        )
    }

    private fun requestAndAssert(
        httpMethod: HttpMethod,
        path: String,
        requestEntity: String? = null,
        expectedResponse: String?,
        expectedCode: HttpStatusCode,
        leggTilCookie: Boolean = true,
        cookie: Cookie = getAuthCookie(fnr)
    ) {
        with(engine) {
            handleRequest(httpMethod, path) {
                if (leggTilCookie) addHeader(HttpHeaders.Cookie, cookie.toString())
                logger.info("Request Entity = $requestEntity")
                addHeader(HttpHeaders.Accept, "application/json")
                if (requestEntity != null) addHeader(HttpHeaders.ContentType, "application/json")
                if (requestEntity != null) setBody(requestEntity)
            }.apply {
                logger.info("Response Entity = ${response.content}")
                logger.info("Expected Entity = $expectedResponse")
                assertEquals(expectedCode, response.status())
                if (expectedResponse != null) {
                    JSONAssert.assertEquals(expectedResponse, response.content!!, true)
                } else {
                    assertEquals(expectedResponse, response.content)
                }
            }
        }
    }
}
