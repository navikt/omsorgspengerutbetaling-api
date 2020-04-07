package no.nav.omsorgspengerutbetaling

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.omsorgspengerutbetaling.soknad.*
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals

internal class SerDesTest {

    @Test
    internal fun `Test reserialisering av request`() {
        JSONAssert.assertEquals(SøknadJson, søknad.somJson(), true)
        assertEquals(søknad, SøknadUtils.objectMapper.readValue(SøknadJson))
    }

    @Test
    fun `Test serialisering av request til mottak`() {
        JSONAssert.assertEquals(KomplettSøknadJson, komplettSøknad.somJson(), true)
        assertEquals(komplettSøknad, SøknadUtils.objectMapper.readValue(KomplettSøknadJson))
    }

    private companion object {
        val now = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
        internal val start = LocalDate.parse("2020-01-01")

        internal val søknad = SøknadUtils.defaultSøknad.copy(
            utbetalingsperioder = listOf(
                UtbetalingsperiodeMedVedlegg(
                    fraOgMed = start,
                    tilOgMed = start.plusDays(10),
                    legeerklæringer = listOf(URI("http://localhost:8080/vedlegg/1"))
                ),
                UtbetalingsperiodeMedVedlegg(
                    fraOgMed = start.plusDays(20),
                    tilOgMed = start.plusDays(20),
                    lengde = Duration.ofHours(5).plusMinutes(30),
                    legeerklæringer = listOf(
                        URI("http://localhost:8080/vedlegg/2"),
                        URI("http://localhost:8080/vedlegg/3")
                    )
                ),
                UtbetalingsperiodeMedVedlegg(
                    fraOgMed = start.plusDays(30),
                    tilOgMed = start.plusDays(35)
                )
            ),
            frilans = Frilans(
                startdato = start,
                jobberFortsattSomFrilans = JaNei.Ja
            ),
            selvstendigVirksomheter = listOf(
                Virksomhet(
                    næringstyper = listOf(
                        Næringstyper.JORDBRUK_SKOGBRUK,
                        Næringstyper.FISKE,
                        Næringstyper.DAGMAMMA,
                        Næringstyper.ANNEN
                    ),
                    fiskerErPåBladB = JaNei.Ja,
                    fraOgMed = start,
                    tilOgMed = start.plusDays(10),
                    næringsinntekt = 100000,
                    navnPåVirksomheten = "Test",
                    organisasjonsnummer = "111",
                    registrertINorge = JaNei.Nei,
                    registrertILand = "Tyskland",
                    yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(
                        oppstartsdato = start.minusYears(2)
                    ),
                    varigEndring = VarigEndring(
                        dato = start.minusYears(1),
                        inntektEtterEndring = 1337,
                        forklaring = "Fordi"
                    ),
                    regnskapsfører = Regnskapsfører(
                        navn = "Regn",
                        telefon = "555-FILK"
                    ),
                    revisor = Revisor(
                        navn = "Rev",
                        telefon = "555-FILM",
                        kanInnhenteOpplysninger = JaNei.Nei
                    )
                )
            )
        )
        internal val komplettSøknad = SøknadUtils.defaultKomplettSøknad.copy(
            mottatt = now
        )

        internal val SøknadJson = """
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
                "lengde": null,
                "legeerklæringer": ["http://localhost:8080/vedlegg/1"]
            }, {
                "fraOgMed": "2020-01-21",
                "tilOgMed": "2020-01-21",
                "lengde": "PT5H30M",
                "legeerklæringer": ["http://localhost:8080/vedlegg/2", "http://localhost:8080/vedlegg/3"]
            }, {
                "fraOgMed": "2020-01-31",
                "tilOgMed": "2020-02-05",
                "lengde": null,
                "legeerklæringer": []
            }],
            "andreUtbetalinger": ["dagpenger", "sykepenger"],
            "frilans": {
                "startdato": "2020-01-01",
                "jobberFortsattSomFrilans": true
            },
            "selvstendigVirksomheter": [{
                "næringstyper": ["JORDBRUK_SKOGBRUK", "FISKE", "DAGMAMMA", "ANNEN"],
                "fiskerErPåBladB": true,
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
                    "telefon": "555-FILK"
                },
                "revisor": {
                    "navn": "Rev",
                    "telefon": "555-FILM",
                    "kanInnhenteOpplysninger": false
                }
            }],
            "erArbeidstakerOgså": true,
            "fosterbarn": [{
                "fødselsnummer": "02119970078"
            }]
        }
        """.trimIndent()

        internal val KomplettSøknadJson = """
        {
            "mottatt": "2018-01-02T03:04:05.000000006Z",
            "språk": "nb",
            "søker": {
                "aktørId": "123456",
                "fødselsdato": "1999-11-02",
                "fødselsnummer": "02119970078",
                "fornavn": "Ola",
                "mellomnavn": null,
                "etternavn": "Nordmann",
                "myndig": true
            },
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
                "lengde": "PT7H30M"
            }],
            "andreUtbetalinger": ["dagpenger", "sykepenger"],
            "frilans": {
                "startdato": "2020-01-01",
                "jobberFortsattSomFrilans": true
            },
            "selvstendigVirksomheter": [{
                "næringstyper": ["JORDBRUK_SKOGBRUK", "FISKE", "DAGMAMMA", "ANNEN"],
                "fiskerErPåBladB": false,
                "fraOgMed": "2019-12-31",
                "tilOgMed": "2020-01-01",
                "næringsinntekt": 123123,
                "navnPåVirksomheten": "TullOgTøys",
                "organisasjonsnummer": "101010",
                "registrertINorge": false,
                "registrertILand": "Tyskland",
                "yrkesaktivSisteTreFerdigliknedeÅrene": {
                    "oppstartsdato": "2020-01-01"
                },
                "varigEndring": {
                    "dato": "2020-01-01",
                    "inntektEtterEndring": 1337,
                    "forklaring": "Fordi"
                },
                "regnskapsfører": {
                    "navn": "Kjell",
                    "telefon": "84554"
                },
                "revisor": {
                    "navn": "Kjell",
                    "telefon": "12345678",
                    "kanInnhenteOpplysninger": true
                }
            }],
            "erArbeidstakerOgså": true,
            "fosterbarn": [{
                "fødselsnummer": "02119970078"
            }],
            "vedlegg": []
        }
        """.trimIndent()
    }
}
