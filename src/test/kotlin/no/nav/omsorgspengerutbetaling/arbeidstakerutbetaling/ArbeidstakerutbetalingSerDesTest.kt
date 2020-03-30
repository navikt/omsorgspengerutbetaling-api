package no.nav.omsorgspengerutbetaling.arbeidstakerutbetaling

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.omsorgspengerutbetaling.felles.UtbetalingsperiodeMedVedlegg
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals

internal class ArbeidstakerutbetalingSerDesTest {

    @Test
    internal fun `Test reserialisering av request`() {
        JSONAssert.assertEquals(SøknadJson, søknad.somJson(), true)
        assertEquals(
            søknad, ArbeidstakerutbetalingSøknadUtils.objectMapper.readValue(
                SøknadJson
            )
        )
    }

    @Test
    fun `Test serialisering av request til mottak`() {
        JSONAssert.assertEquals(KomplettSøknadJson, komplettSøknad.somJson(), true)
        assertEquals(
            komplettSøknad, ArbeidstakerutbetalingSøknadUtils.objectMapper.readValue(
                KomplettSøknadJson
            )
        )
    }

    private companion object {
        val now = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
        internal val start = LocalDate.parse("2020-01-01")

        internal val søknad = ArbeidstakerutbetalingSøknadUtils.defaultSøknad.copy(
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
            )
        )
        internal val komplettSøknad = ArbeidstakerutbetalingSøknadUtils.defaultKomplettSøknad.copy(
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
            "fosterbarn": [{
                "fødselsnummer": "02119970078",
                "fornavn": "fornavn",
                "etternavn": "etternavn"
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
            "fosterbarn": [{
                "fødselsnummer": "02119970078",
                "fornavn": "fornavn",
                "etternavn": "etternavn"
            }]
        }
        """.trimIndent()
    }
}
