package no.nav.omsorgspengerutbetaling

import KafkaWrapper
import com.github.fppt.jedismock.RedisServer
import com.github.tomakehurst.wiremock.http.Cookie
import com.typesafe.config.ConfigFactory
import hentSøknad
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.ktor.core.fromResources
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.omsorgspengerutbetaling.SøknadUtils.hentGyldigSøknad
import no.nav.omsorgspengerutbetaling.felles.BARN_URL
import no.nav.omsorgspengerutbetaling.felles.SØKER_URL
import no.nav.omsorgspengerutbetaling.felles.SØKNAD_URL
import no.nav.omsorgspengerutbetaling.felles.somJson
import no.nav.omsorgspengerutbetaling.mellomlagring.started
import no.nav.omsorgspengerutbetaling.soknad.*
import no.nav.omsorgspengerutbetaling.wiremock.*
import org.json.JSONObject
import org.junit.AfterClass
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import testConsumer
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val fnr = "290990123456"
private const val ikkeMyndigFnr = "12125012345"

// Se https://github.com/navikt/dusseldorf-ktor#f%C3%B8dselsnummer
private val gyldigFodselsnummerA = "02119970078"
private val ikkeMyndigDato = "2050-12-12"

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
            .stubOppslagHealth()
            .stubK9OppslagSoker()
            .stubK9OppslagBarn()
            .stubK9Mellomlagring()

        private val kafkaEnvironment = KafkaWrapper.bootstrap()
        private val kafkaKonsumer = kafkaEnvironment.testConsumer()

        val redisServer: RedisServer = RedisServer
            .newRedisServer()
            .started()

        fun getConfig(): ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap(
                    wireMockServer = wireMockServer,
                    kafkaEnvironment = kafkaEnvironment,
                    redisServer = redisServer
                )
            )
            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }


        val engine = TestApplicationEngine(createTestEnvironment {
            config = getConfig()
        }).apply {
            start(wait = true)
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
            path = SØKER_URL,
            expectedCode = HttpStatusCode.OK,
            expectedResponse = expectedGetSokerJson(fnr)
        )
    }

    @Test
    fun `Hente søker hvor man får 451 fra oppslag`() {
        wireMockServer.stubK9OppslagSoker(
            statusCode = HttpStatusCode.fromValue(451),
            responseBody =
            //language=json
            """
            {
                "detail": "Policy decision: DENY - Reason: (NAV-bruker er i live AND NAV-bruker er ikke myndig)",
                "instance": "/meg",
                "type": "/problem-details/tilgangskontroll-feil",
                "title": "tilgangskontroll-feil",
                "status": 451
            }
            """.trimIndent()
        )

        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = SØKER_URL,
            expectedCode = HttpStatusCode.fromValue(451),
            expectedResponse =
            //language=json
            """
            {
                "type": "/problem-details/tilgangskontroll-feil",
                "title": "tilgangskontroll-feil",
                "status": 451,
                "instance": "/soker",
                "detail": "Tilgang nektet."
            }
            """.trimIndent(),
            cookie = getAuthCookie(ikkeMyndigFnr)
        )

        wireMockServer.stubK9OppslagSoker()
    }

    @Test
    fun `Hente søker som ikke er myndig`() {
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = SØKER_URL,
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
    fun `Hente barn og sjekk eksplisit at identitetsnummer ikke blir med ved get kall`(){

        val respons = requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = BARN_URL,
            expectedCode = HttpStatusCode.OK,
            //language=json
            expectedResponse = """
                {
                  "barn": [
                    {
                      "fødselsdato": "2000-08-27",
                      "fornavn": "BARN",
                      "mellomnavn": "EN",
                      "etternavn": "BARNESEN",
                      "aktørId": "1000000000001"
                    },
                    {
                      "fødselsdato": "2001-04-10",
                      "fornavn": "BARN",
                      "mellomnavn": "TO",
                      "etternavn": "BARNESEN",
                      "aktørId": "1000000000002"
                    }
                  ]
                }
            """.trimIndent()
        )

        val responsSomJSONArray = JSONObject(respons).getJSONArray("barn")

        assertFalse(responsSomJSONArray.getJSONObject(0).has("identitetsnummer"))
        assertFalse(responsSomJSONArray.getJSONObject(1).has("identitetsnummer"))
    }

    @Test
    fun `Feil ved henting av barn skal returnere tom liste`() {
        wireMockServer.stubK9OppslagBarn(simulerFeil = true)
        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = BARN_URL,
            expectedCode = HttpStatusCode.OK,
            expectedResponse = """
            {
                "barn": []
            }
            """.trimIndent(),
            cookie = getAuthCookie("13106423495")
        )
        wireMockServer.stubK9OppslagBarn()
    }

    @Test
    fun `Hente barn hvor man får 451 fra oppslag`(){
        wireMockServer.stubK9OppslagBarn(
            statusCode = HttpStatusCode.fromValue(451),
            responseBody =
            //language=json
            """
            {
                "detail": "Policy decision: DENY - Reason: (NAV-bruker er i live AND NAV-bruker er ikke myndig)",
                "instance": "/meg",
                "type": "/problem-details/tilgangskontroll-feil",
                "title": "tilgangskontroll-feil",
                "status": 451
            }
            """.trimIndent()
        )

        requestAndAssert(
            httpMethod = HttpMethod.Get,
            path = BARN_URL,
            expectedCode = HttpStatusCode.fromValue(451),
            expectedResponse =
            //language=json
            """
            {
                "type": "/problem-details/tilgangskontroll-feil",
                "title": "tilgangskontroll-feil",
                "status": 451,
                "instance": "/barn",
                "detail": "Tilgang nektet."
            }
            """.trimIndent(),
            cookie = getAuthCookie(ikkeMyndigFnr)
        )

        wireMockServer.stubK9OppslagBarn() // reset til default mapping
    }

    @Test
    fun `Sende søknad`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        val søknad = hentGyldigSøknad().copy(
            vedlegg = listOf(URL(engine.jpegUrl(cookie)), URL(engine.pdUrl(cookie)))
        )

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = søknad.somJson()
        )

        hentOgAssertSøknad(JSONObject(søknad))
    }

    @Test
    fun `Sende søknad uten identitetsnummer på barn og verifisere at barn har fått identitetsnummer`() {
        val cookie = getAuthCookie(fnr)

        val søknad = hentGyldigSøknad().copy(
            vedlegg = listOf(URL(engine.jpegUrl(cookie)), URL(engine.pdUrl(cookie))),
            barn = listOf(
                Barn(
                    navn = "Barn Barnesen",
                    fødselsdato = LocalDate.parse("2021-01-01"),
                    aktørId = "1000000000001",
                    identitetsnummer = null
                )
            )
        )

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = søknad.somJson()
        )

        hentOgAssertSøknad(JSONObject(søknad))

        val hentetSøknad = kafkaKonsumer.hentSøknad(søknad.søknadId.id)

        assertTrue(hentetSøknad.data.getJSONArray("barn").getJSONObject(0).getString("identitetsnummer") != null)
    }

    @Test
    fun `Sende søknad med selvstendigNæringsdrivende og ikke noe for selvstendigVirksomheter`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        val søknad = hentGyldigSøknad().copy(
            vedlegg = listOf(URL(engine.jpegUrl(cookie)), URL(engine.pdUrl(cookie))),
            selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                fraOgMed = LocalDate.parse("2020-01-10"),
                tilOgMed = LocalDate.parse("2021-01-10"),
                næringsinntekt = 123123,
                navnPåVirksomheten = "TullOgTøys",
                registrertINorge = JaNei.Nei,
                registrertIUtlandet = Land(
                    landkode = "DEU",
                    landnavn = "Tyskland"
                ),
                erNyoppstartet = true,
                harFlereAktiveVirksomheter = false
            )
        )

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = søknad.somJson()
        )

        hentOgAssertSøknad(JSONObject(søknad))
    }

    @Test
    fun `Sende søknad ikke innlogget`() {
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedCode = HttpStatusCode.Unauthorized,
            expectedResponse = null,
            requestEntity = hentGyldigSøknad().somJson(),
            leggTilCookie = false
        )
    }

    @Test
    fun `Sende soknad ikke myndig`() {
        val cookie = getAuthCookie(ikkeMyndigFnr)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse =
            //language=json
            """
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
            requestEntity = hentGyldigSøknad().somJson()
        )
    }

    @Test
    fun `Sende soknad hvor et av vedleggene peker på et ikke eksisterende vedlegg`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)
        val finnesIkkeUrl = jpegUrl.substringBeforeLast("/").plus("/").plus(UUID.randomUUID().toString())

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse =
            //language=json
            """
            {
                "type": "/problem-details/invalid-request-parameters",
                "title": "invalid-request-parameters",
                "status": 400,
                "detail": "Requesten inneholder ugyldige paramtere.",
                "instance": "about:blank",
                "invalid_parameters": [{
                    "type": "entity",
                    "name": "vedlegg",
                    "reason": "Mottok referanse til 2 vedlegg, men fant kun 1 vedlegg.",
                    "invalid_value": ["$jpegUrl", "$finnesIkkeUrl"]
                }]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = hentGyldigSøknad().copy(
                vedlegg = listOf(URL(jpegUrl), URL(finnesIkkeUrl))
            ).somJson()
        )
    }

    @Test
    fun `Sende soknad hvor antallTimerPlanlagt er satt men ikke antallTimerBorte`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse =
            //language=json
            """
            {
              "type": "/problem-details/invalid-request-parameters",
              "title": "invalid-request-parameters",
              "status": 400,
              "detail": "Requesten inneholder ugyldige paramtere.",
              "instance": "about:blank",
              "invalid_parameters": [
                {
                  "type": "entity",
                  "name": "utbetalingsperioder[0]",
                  "reason": "Dersom antallTimerPlanlagt er satt så kan ikke antallTimerBorte være tom",
                  "invalid_value": "antallTimerBorte = null, antallTimerPlanlagt=PT7H"
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils.hentGyldigSøknad().copy(
                utbetalingsperioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(1),
                        antallTimerPlanlagt = Duration.ofHours(7),
                        aktivitetFravær = listOf(AktivitetFravær.FRILANSER),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
                    )
                )
            ).somJson()
        )
    }

    @Test
    fun `Gitt at ingen aktivitetFravær er oppgitt på utbetalingsperiode, forvent valideringsfeil`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse =
            //language=json
            """
            {
              "type": "/problem-details/invalid-request-parameters",
              "title": "invalid-request-parameters",
              "status": 400,
              "detail": "Requesten inneholder ugyldige paramtere.",
              "instance": "about:blank",
              "invalid_parameters": [
                {
                  "type": "entity",
                  "name": "fraværsperioder[0]",
                  "reason": "Aktivitet må være satt",
                  "invalid_value": "k9-format feilkode: påkrevd"
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils.hentGyldigSøknad().copy(
                utbetalingsperioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(1),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
                    )
                )
            ).somJson()
        )
    }

    @Test
    fun `Sende soknad hvor antallTimerPlanlagt er mindre enn antallTimerBorte`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse =
            //language=json
            """
            {
              "type": "/problem-details/invalid-request-parameters",
              "title": "invalid-request-parameters",
              "status": 400,
              "detail": "Requesten inneholder ugyldige paramtere.",
              "instance": "about:blank",
              "invalid_parameters": [
                {
                  "type": "entity",
                  "name": "utbetalingsperioder[0]",
                  "reason": "Antall timer borte kan ikke være større enn antall timer planlagt jobbe",
                  "invalid_value": "antallTimerBorte = PT8H, antallTimerPlanlagt=PT7H"
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = SøknadUtils.hentGyldigSøknad().copy(
                utbetalingsperioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(1),
                        antallTimerPlanlagt = Duration.ofHours(7),
                        antallTimerBorte = Duration.ofHours(8),
                        aktivitetFravær = listOf(AktivitetFravær.SELVSTENDIG_VIRKSOMHET),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
                    )
                )
            ).somJson()
        )
    }

    @Test
    fun `Sende soknad med ugylidge parametre gir feil`() {
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedCode = HttpStatusCode.BadRequest,
            requestEntity = hentGyldigSøknad().copy(
                bekreftelser = Bekreftelser(
                    harForståttRettigheterOgPlikter = JaNei.Nei,
                    harBekreftetOpplysninger = JaNei.Nei
                ),
                spørsmål = listOf(),
                frilans = null,
                selvstendigNæringsdrivende = null
            ).somJson(),
            expectedResponse =
            //language=json
            """
            {
              "type": "/problem-details/invalid-request-parameters",
              "title": "invalid-request-parameters",
              "status": 400,
              "detail": "Requesten inneholder ugyldige paramtere.",
              "instance": "about:blank",
              "invalid_parameters": [
                {
                  "type": "entity",
                  "name": "bekreftlser.harBekreftetOpplysninger",
                  "reason": "Må besvars Ja.",
                  "invalid_value": false
                },
                {
                  "type": "entity",
                  "name": "bekreftelser.harForståttRettigheterOgPlikter",
                  "reason": "Må besvars Ja.",
                  "invalid_value": false
                },
                {
                  "type": "entity",
                  "name": "frilans/selvstendigNæringsdrivende",
                  "reason": "Må settes 'frilans' eller 'selvstendigNæringsdrivende'",
                  "invalid_value": null
                }
              ]
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
    fun `Sende søknad med ugyldig andreUtbetalinger`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse =
            //language=json
            """
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
                      "reason": "Ugyldig verdi for annen utbetaling. Kun 'dagpenger', 'sykepenger' og 'midlertidigkompensasjonsnfri' er tillatt.",
                      "invalid_value": "koronapenger"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = hentGyldigSøknad().copy(
                andreUtbetalinger = listOf("sykepenger", "koronapenger")
            ).somJson()
        )
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
    ): String? {
        val respons: String?
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
                respons = response.content
                assertEquals(expectedCode, response.status())
                if (expectedResponse != null) {
                    JSONAssert.assertEquals(expectedResponse, response.content!!, true)
                } else {
                    assertEquals(expectedResponse, response.content)
                }
            }
        }
        return respons
    }

    @Test
    fun `Sende søknad med selvstendig næringsvirksomhet som ikke er gyldig, mangler registrertIUtlandet`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse =
            //language=json
            """
            {
              "type": "/problem-details/invalid-request-parameters",
              "title": "invalid-request-parameters",
              "status": 400,
              "detail": "Requesten inneholder ugyldige paramtere.",
              "instance": "about:blank",
              "invalid_parameters": [
                {
                  "type": "entity",
                  "name": "selvstendigNæringsdrivende.perioder{2021-02-07-2021-02-08}.landkode",
                  "reason": "landkode må være satt, og kan ikke være null, dersom virksomhet er registrert i utlandet.",
                  "invalid_value": "k9-format feilkode: påkrevd"
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = hentGyldigSøknad().copy(
                selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                    næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                    fraOgMed = LocalDate.parse("2021-02-07"),
                    tilOgMed = LocalDate.parse("2021-02-08"),
                    næringsinntekt = 1233123,
                    navnPåVirksomheten = "TullOgTøys",
                    registrertINorge = JaNei.Nei,
                    registrertIUtlandet = null,
                    organisasjonsnummer = "916974574",
                    yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now()),
                    regnskapsfører = Regnskapsfører(
                        navn = "Kjell",
                        telefon = "84554"
                    ),
                    fiskerErPåBladB = JaNei.Nei,
                    erNyoppstartet = true,
                    harFlereAktiveVirksomheter = true
                )
            ).somJson()
        )
    }

    @Test
    fun `Sende søknad med selvstendig næringsvirksomhet med ugyldig registrertIUtlandet`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse =
            //language=json
            """
            {
              "type": "/problem-details/invalid-request-parameters",
              "title": "invalid-request-parameters",
              "status": 400,
              "detail": "Requesten inneholder ugyldige paramtere.",
              "instance": "about:blank",
              "invalid_parameters": [
                {
                  "type": "entity",
                  "name": "selvstendigNæringsdrivende.landkode",
                  "reason": "Landkode er ikke en gyldig ISO 3166-1 alpha-3 kode.",
                  "invalid_value": "ukjent"
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = hentGyldigSøknad().copy(
                selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                    næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                    fraOgMed = LocalDate.parse("2021-02-07"),
                    tilOgMed = LocalDate.parse("2021-02-08"),
                    næringsinntekt = 1233123,
                    navnPåVirksomheten = "TullOgTøys",
                    registrertINorge = JaNei.Nei,
                    registrertIUtlandet = Land(
                        landkode = "ukjent",
                        landnavn = "ukjent"
                    ),
                    organisasjonsnummer = "101010",
                    yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now()),
                    regnskapsfører = Regnskapsfører(
                        navn = "Kjell",
                        telefon = "84554"
                    ),
                    fiskerErPåBladB = JaNei.Nei,
                    erNyoppstartet = true,
                    harFlereAktiveVirksomheter = true
                )
            ).somJson()
        )
    }

    @Test
    fun `Sende søknad med selvstendig næringsvirksomhet som ikke har gyldig organisasjonsnummer`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse =
            //language=json
            """
            {
              "type": "/problem-details/invalid-request-parameters",
              "title": "invalid-request-parameters",
              "status": 400,
              "detail": "Requesten inneholder ugyldige paramtere.",
              "instance": "about:blank",
              "invalid_parameters": [
                {
                  "type": "entity",
                  "name": "selvstendigNæringsdrivende.organisasjonsnummer",
                  "reason": "Ugyldig organisasjonsnummer, inneholder noe annet enn tall.",
                  "invalid_value": null
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = hentGyldigSøknad().copy(
                selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                    næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                    fraOgMed = LocalDate.now().minusDays(1),
                    tilOgMed = LocalDate.now(),
                    næringsinntekt = 1233123,
                    navnPåVirksomheten = "TullOgTøys",
                    organisasjonsnummer = "123a",
                    registrertINorge = JaNei.Ja,
                    yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now()),
                    regnskapsfører = Regnskapsfører(
                        navn = "Kjell",
                        telefon = "84554"
                    ),
                    fiskerErPåBladB = JaNei.Nei,
                    erNyoppstartet = true,
                    harFlereAktiveVirksomheter = true
                )

            ).somJson()
        )
    }

    @Test
    fun `Sende søknad ugyldig fødselsnummer på fosterbarn, gir feilmelding`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse =
            //language=json
            """
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
                      "reason": "Ugyldig fødselsnummer",
                      "invalid_value": null
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = hentGyldigSøknad().copy(
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
    fun `Sende søknad med frilanser som har sluttet, uten sluttdato, gir feilmelding`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse =
            //language=json
            """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "frilans.sluttdato",
                      "reason": "Sluttdato kan ikke være null dersom jobberFortsattSomFrilans=Nei",
                      "invalid_value": "sluttdato=null,jobberFortsattSomFrilans=Nei "
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = hentGyldigSøknad().copy(
                frilans = Frilans(
                    startdato = LocalDate.now(),
                    sluttdato = null,
                    jobberFortsattSomFrilans = JaNei.Nei
                )
            ).somJson()
        )
    }

    @Test
    fun `Sende søknad med frilanser der startdato er etter sluttdato, gir feilmelding`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = SØKNAD_URL,
            expectedResponse =
            //language=json
            """
            {
              "type": "/problem-details/invalid-request-parameters",
              "title": "invalid-request-parameters",
              "status": 400,
              "detail": "Requesten inneholder ugyldige paramtere.",
              "instance": "about:blank",
              "invalid_parameters": [
                {
                  "type": "entity",
                  "name": "frilans.sluttdato",
                  "reason": "Sluttdato kan ikke være før startdato",
                  "invalid_value": "startdato=2021-02-01, sluttdato=2021-01-01"
                }
              ]
            }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = hentGyldigSøknad().copy(
                frilans = Frilans(
                    startdato = LocalDate.parse("2021-02-01"),
                    sluttdato = LocalDate.parse("2021-01-01"),
                    jobberFortsattSomFrilans = JaNei.Nei
                )
            ).somJson()
        )
    }

    private fun hentOgAssertSøknad(søknad: JSONObject) {
        val hentet = kafkaKonsumer.hentSøknad(søknad.getJSONObject("søknadId").getString("id"))
        assertGyldigSøknad(søknad, hentet.data)
    }

    private fun assertGyldigSøknad(
        søknadSendtInn: JSONObject,
        søknadFraTopic: JSONObject
    ) {
        assertTrue(søknadFraTopic.has("søker"))
        assertTrue(søknadFraTopic.has("mottatt"))
        assertTrue(søknadFraTopic.has("k9FormatSøknad"))

        assertEquals(søknadSendtInn.getJSONArray("vedlegg").length() ,søknadFraTopic.getJSONArray("vedleggId").length())
        assertEquals(søknadSendtInn.getJSONArray("andreUtbetalinger").toString(), søknadFraTopic.getJSONArray("andreUtbetalinger").toString())
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


