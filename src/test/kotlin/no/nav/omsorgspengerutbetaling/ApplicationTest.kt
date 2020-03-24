package no.nav.omsorgspengerutbetaling

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
import no.nav.helse.dusseldorf.ktor.core.fromResources
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.omsorgspengerutbetaling.mellomlagring.started
import no.nav.omsorgspengerutbetaling.soknad.*
import no.nav.omsorgspengerutbetaling.wiremock.*
import org.junit.AfterClass
import org.junit.BeforeClass
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val fnr = "290990123456"
private const val ikkeMyndigFnr = "12125012345"

// Se https://github.com/navikt/dusseldorf-ktor#f%C3%B8dselsnummer
private val gyldigFodselsnummerA = "02119970078"
private val ikkeMyndigDato = "2050-12-12"

@KtorExperimentalAPI
class ApplicationTest {

    private companion object {

        private val logger: Logger = LoggerFactory.getLogger(ApplicationTest::class.java)

        val wireMockServer = WireMockBuilder()
            .withAzureSupport()
            .withNaisStsSupport()
            .withLoginServiceSupport()
            .omsorgspengesoknadApiConfig()
            .build()
            .stubK9DokumentHealth()
            .stubOmsorgspengerutbetalingsoknadMottakHealth()
            .stubOppslagHealth()
            .stubLeggSoknadTilProsessering()
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
    fun `test isready, isalive, health og metrics`() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/isready") {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                handleRequest(HttpMethod.Get, "/isalive") {}.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    handleRequest(HttpMethod.Get, "/metrics") {}.apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        handleRequest(HttpMethod.Get, "/health") {}.apply {
                            assertEquals(HttpStatusCode.OK, response.status())
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `Hente søker`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/soker",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = expectedGetSokerJson(fnr)
        )
    }

    @Test
    fun `Hente søker som ikke er myndig`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = "/soker",
            expectedCode = HttpStatusCode.OK,
            expectedResponse = expectedGetSokerJson(
                fodselsnummer = ikkeMyndigFnr,
                fodselsdato = ikkeMyndigDato,
                myndig = false
            ),
            cookie = getAuthCookie(ikkeMyndigFnr)
        )
    }

    @Test
    fun `Sende soknad`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)
        val pdfUrl = engine.pdUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = SøknadUtils.defaultSøknad.copy(
                spørsmål = listOf(
                    SpørsmålOgSvar(
                        spørsmål = "Spørsmål 1",
                        svar = JaNei.Ja
                    )
                ),
                utbetalingsperioder = listOf(
                    UtbetalingsperiodeMedVedlegg(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now(),
                        lengde = Duration.ofHours(6),
                        legeerklæringer = listOf(URI(jpegUrl), URI(pdfUrl))
                    ),
                    UtbetalingsperiodeMedVedlegg(
                        fraOgMed = LocalDate.now().plusDays(10),
                        tilOgMed = LocalDate.now().plusDays(15),
                        legeerklæringer = listOf()
                    )
                )
            ).somJson()
        )
    }

    @Test
    fun `Sende soknad som raw json`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)
        val pdfUrl = engine.pdUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                "lengde": null,
                "legeerklæringer": []
            }],
            "frilans": {
                "startdato": "2020-01-01",
                "jobberFortsattSomFrilans": true
            },
            "selvstendigVirksomheter": [{
                "næringstyper": ["JORDBRUK_SKOGBRUK", "FISKE", "DAGMAMMA", "ANNEN"],
                "fiskerErPåBladB": false,
                "fraOgMed": "2020-01-01",
                "tilOgMed": "2020-01-11",
                "næringsinntekt": 100000,
                "navnPåVirksomheten": "Test",
                "organisasjonsnummer": "111",
                "registrertINorge": false,
                "registrertILand": "Tyskland",
                "yrkesaktivSisteTreFerdigliknedeÅrene": {
                    "oppstartsdato": "2018-01-01"
                },
                "varigEndring": {
                    "dato": "2019-01-01",
                    "inntektEtterEndring": 1337,
                    "forklaring": "Fordi"
                },
                "regnskapsfører": {
                    "navn": "Regn",
                    "telefon": "555-FILK",
                    "erNærVennFamilie": false
                },
                "revisor": {
                    "navn": "Rev",
                    "telefon": "555-FILM",
                    "erNærVennFamilie": true,
                    "kanInnhenteOpplysninger": false
                }
            }]
        }
            """.trimIndent()
        )
    }

    @Test
    fun `Sende søknad ikke innlogget`() {
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedCode = HttpStatusCode.Unauthorized,
            expectedResponse = null,
            requestEntity = SøknadUtils.defaultSøknad.somJson(),
            leggTilCookie = false
        )
    }

    @Test
    fun `Sende soknad ikke myndig`() {
        val cookie = getAuthCookie(ikkeMyndigFnr)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
            requestEntity = SøknadUtils.defaultSøknad.somJson()
        )
    }

    @Test
    fun `Sende soknad hvor et av vedleggene peker på et ikke eksisterende vedlegg`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)
        val finnesIkkeUrl = jpegUrl.substringBeforeLast("/").plus("/").plus(UUID.randomUUID().toString())

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                    "reason": "Mottok referanse til 2 vedlegg, men fant kun 1 vedlegg.",
                    "invalid_value": ["$jpegUrl", "$finnesIkkeUrl"]
                }]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils.defaultSøknad.copy(
                utbetalingsperioder = listOf(
                    UtbetalingsperiodeMedVedlegg(
                        fraOgMed = LocalDate.now().plusDays(10),
                        tilOgMed = LocalDate.now().plusDays(15),
                        legeerklæringer = listOf(
                            URI(jpegUrl),
                            URI(finnesIkkeUrl)
                        )
                    )
                )
            ).somJson()
        )
    }

    @Test
    fun `Sende soknad med ugylidge parametre gir feil`() {
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedCode = HttpStatusCode.BadRequest,
            requestEntity = SøknadUtils.defaultSøknad.copy(
                bekreftelser = Bekreftelser(
                    harForståttRettigheterOgPlikter = JaNei.Nei,
                    harBekreftetOpplysninger = JaNei.Nei
                ),
                spørsmål = listOf(),
                utbetalingsperioder = listOf(),
                selvstendigVirksomheter = listOf(),
                frilans = null
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
                },
                {
                    "type": "entity",
                    "name": "frilans/selvstendigVirksomheter",
                    "reason": "Må settes 'frilans' eller minst en 'selvstendigVirksomheter'",
                    "invalid_value": null
                }]
            }
            """.trimIndent()
        )
    }

    @Test
    fun `Test haandtering av vedlegg`() {
        val cookie = getAuthCookie(fnr)
        val jpeg = "vedlegg/iPhone_6.jpg".fromResources().readBytes()

        with(engine) {
            // LASTER OPP VEDLEGG
            val url = handleRequestUploadImage(
                cookie = cookie,
                vedlegg = jpeg
            )
            val path = Url(url).fullPath
            // HENTER OPPLASTET VEDLEGG
            handleRequest(HttpMethod.Get, path) {
                addHeader("Cookie", cookie.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(Arrays.equals(jpeg, response.byteContent))
                // SLETTER OPPLASTET VEDLEGG
                handleRequest(HttpMethod.Delete, path) {
                    addHeader("Cookie", cookie.toString())
                }.apply {
                    assertEquals(HttpStatusCode.NoContent, response.status())
                    // VERIFISERER AT VEDLEGG ER SLETTET
                    handleRequest(HttpMethod.Get, path) {
                        addHeader("Cookie", cookie.toString())
                    }.apply {
                        assertEquals(HttpStatusCode.NotFound, response.status())
                    }
                }
            }
        }
    }

    @Test
    fun `Test opplasting av ikke støttet vedleggformat`() {
        engine.handleRequestUploadImage(
            cookie = getAuthCookie(gyldigFodselsnummerA),
            vedlegg = "jwkset.json".fromResources().readBytes(),
            contentType = "application/json",
            fileName = "jwkset.json",
            expectedCode = HttpStatusCode.BadRequest
        )
    }

    @Test
    fun `Test opplasting av for stort vedlegg`() {
        engine.handleRequestUploadImage(
            cookie = getAuthCookie(gyldigFodselsnummerA),
            vedlegg = ByteArray(8 * 1024 * 1024 + 10),
            contentType = "image/png",
            fileName = "big_picture.png",
            expectedCode = HttpStatusCode.PayloadTooLarge
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

    @Test
    fun `Sende søknad med selvstendig næringsvirksomhet som ikke er gyldig, mangler registrertILand`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
                      "name": "registrertILand",
                      "reason": "Hvis registrertINorge er false så må registrertILand være satt til noe",
                      "invalid_value": null
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils.defaultSøknad.copy(
                selvstendigVirksomheter = listOf(
                    Virksomhet(
                        næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                        fraOgMed = LocalDate.now().minusDays(1),
                        tilOgMed = LocalDate.now(),
                        næringsinntekt = 1233123,
                        navnPåVirksomheten = "TullOgTøys",
                        registrertINorge = JaNei.Nei,
                        organisasjonsnummer = "101010",
                        yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now()),
                        regnskapsfører = Regnskapsfører(
                            navn = "Kjell",
                            telefon = "84554"
                        ),
                        fiskerErPåBladB = JaNei.Nei
                    )
                )
            ).somJson()
        )
    }

    @Test
    fun `Sende søknad ugyldig fødselsnummer på fosterbarn, gir feilmelding`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
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
            requestEntity = SøknadUtils.defaultSøknad.copy(
                fosterbarn = listOf(
                    FosterBarn(
                        fødselsnummer = "02119970078",
                        fornavn = "Ole",
                        etternavn = "Nordmann"
                    ),
                    FosterBarn(
                        fødselsnummer = "ugyldig fødselsnummer",
                        fornavn = "Ole",
                        etternavn = "Nordmann"
                    )
                )
            ).somJson()
        )
    }


    private fun expectedGetSokerJson(
        fodselsnummer: String,
        fodselsdato: String = "1997-05-25",
        myndig: Boolean = true
    ) = """
    {
        "etternavn": "MORSEN",
        "fornavn": "MOR",
        "mellomnavn": "HEISANN",
        "fødselsnummer": "$fodselsnummer",
        "aktørId": "12345",
        "fødselsdato": "$fodselsdato",
        "myndig": $myndig
    }
    """.trimIndent()
}
