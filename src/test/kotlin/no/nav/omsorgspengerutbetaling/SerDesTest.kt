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
        JSONAssert.assertEquals(kompletSøknadJson, komplettSøknad.somJson(), true)
        assertEquals(komplettSøknad, SøknadUtils.objectMapper.readValue(kompletSøknadJson))
    }

    private companion object {
        internal val start = LocalDate.parse("2020-01-01")
        internal val komplettSøknad = Søknad(
            språk = Språk.NYNORSK,
            bosteder = listOf(
                Bosted(
                    fraOgMed = start,
                    tilOgMed = start.plusDays(5),
                    landnavn = "Sverige",
                    landkode = "SWE"
                ),
                Bosted(
                    fraOgMed = start.plusDays(10),
                    tilOgMed = start.plusDays(10),
                    landnavn = "Norge",
                    landkode = "NOR"
                )
            ),
            opphold = listOf(
                Bosted(
                    fraOgMed = start.plusDays(15),
                    tilOgMed = start.plusDays(20),
                    landnavn = "England",
                    landkode = "Eng"
                ),
                Bosted(
                    fraOgMed = start.minusDays(10),
                    tilOgMed = start.minusDays(5),
                    landnavn = "Kroatia",
                    landkode = "CRO"
                )
            ),
            jaNei = listOf(
                JaNei(
                    id = JaNeiId.HarForståttRettigheterOgPlikter,
                    spørsmål = "HarForståttRettigheterOgPlikter?",
                    svar = JaNeiSvar.Ja
                ),
                JaNei(
                    id = JaNeiId.HarBekreftetOpplysninger,
                    spørsmål = "HarBekreftetOpplysninger?",
                    svar = JaNeiSvar.Ja
                ),
                JaNei(
                    spørsmål = "Har du vært hjemme?",
                    svar = JaNeiSvar.Nei
                )
            ),
            utbetalingsperioder = listOf(
                UtbetalingsperiodeUri(
                    fraOgMed = start,
                    tilOgMed = start.plusDays(10),
                    legeerklæringer = listOf(URI("http://localhost:8080/vedlegg/1"))
                ),
                UtbetalingsperiodeUri(
                    fraOgMed = start.plusDays(20),
                    tilOgMed = start.plusDays(20),
                    lengde = Duration.ofHours(5).plusMinutes(30),
                    legeerklæringer = listOf(URI("http://localhost:8080/vedlegg/2"), URI("http://localhost:8080/vedlegg/3"))
                ),
                UtbetalingsperiodeUri(
                    fraOgMed = start.plusDays(30),
                    tilOgMed = start.plusDays(35)
                )
            )
        )
        internal val kompletSøknadJson = """
        {
            "språk": "nn",
            "bosteder": [{
                "fraOgMed": "2020-01-01",
                "tilOgMed": "2020-01-06",
                "landkode": "SWE",
                "landnavn": "Sverige"
            }, {
                "fraOgMed": "2020-01-11",
                "tilOgMed": "2020-01-11",
                "landkode": "NOR",
                "landnavn": "Norge"
            }],
            "opphold": [{
                "fraOgMed": "2020-01-16",
                "tilOgMed": "2020-01-21",
                "landkode": "Eng",
                "landnavn": "England"
            }, {
                "fraOgMed": "2019-12-22",
                "tilOgMed": "2019-12-27",
                "landkode": "CRO",
                "landnavn": "Kroatia"
            }],
            "jaNei": [{
                "id": "HarForståttRettigheterOgPlikter",
                "spørsmål": "HarForståttRettigheterOgPlikter?",
                "svar": "Ja"
            }, {
                "id": "HarBekreftetOpplysninger",
                "spørsmål": "HarBekreftetOpplysninger?",
                "svar": "Ja"
            }, {
                "id": null,
                "spørsmål": "Har du vært hjemme?",
                "svar": "Nei"
            }],
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
            }]
        }
        """.trimIndent()
    }

}