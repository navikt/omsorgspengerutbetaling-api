package no.nav.omsorgspengerutbetaling

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.omsorgspengerutbetaling.soknad.*
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import kotlin.test.assertEquals

internal class SerDesTest {

    @Test
    internal fun `Test reserialisering av request`() {
        JSONAssert.assertEquals(komplettSøknadJson, komplettSøknad.somJson(), true)
        assertEquals(komplettSøknad, SøknadUtils.objectMapper.readValue(komplettSøknadJson))
    }

    private companion object {
        internal val start = LocalDate.parse("2020-01-01")
        internal val komplettSøknad = SøknadUtils.defaultSøknad.copy(
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
                    legeerklæringer = listOf(URI("http://localhost:8080/vedlegg/2"), URI("http://localhost:8080/vedlegg/3"))
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
                    fiskerErPåBladB = null,
                    fraOgMed = start,
                    tilOgMed = start.plusDays(10),
                    næringsinntekt = 100000,
                    navnPaVirksomheten = "Test",
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
                    regnskapsforer = Regnskapsforer(
                        navn = "Regn",
                        telefon = "555-FILK",
                        erNærVennFamilie = JaNei.Nei
                    ),
                    revisor = Revisor(
                        navn ="Rev",
                        telefon = "555-FILM",
                        erNærVennFamilie = JaNei.Ja,
                        kanInnhenteOpplysninger = JaNei.Nei
                    )
                )
            )
        )

        internal val komplettSøknadJson = """
        {
            "språk": "nb",
            "bosteder": [{
                "fraOgMed": "2019-12-12",
                "tilOgMed": "2019-12-22",
                "landkode": "GB",
                "landnavn": "Great Britain"
            }],
            "opphold": [{
                "fraOgMed": "2019-12-12",
                "tilOgMed": "2019-12-22",
                "landkode": "GB",
                "landnavn": "Great Britain"
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
            "frilans": {
                "startdato": "2020-01-01",
                "jobberFortsattSomFrilans": true
            },
            "selvstendigVirksomheter": [{
                "næringstyper": ["JORDBRUK_SKOGBRUK", "FISKE", "DAGMAMMA", "ANNEN"],
                "fiskerErPåBladB": null,
                "fraOgMed": "2020-01-01",
                "tilOgMed": "2020-01-11",
                "næringsinntekt": 100000,
                "navnPaVirksomheten": "Test",
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
                "regnskapsforer": {
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
    }
}