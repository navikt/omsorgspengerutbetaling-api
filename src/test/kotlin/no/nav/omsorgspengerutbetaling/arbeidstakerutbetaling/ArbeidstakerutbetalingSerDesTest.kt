package no.nav.omsorgspengerutbetaling.arbeidstakerutbetaling

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.omsorgspengerutbetaling.felles.Utbetalingsperiode
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

        internal val søknad = ArbeidstakerutbetalingSøknadUtils.defaultSøknad
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
            "jobbHosNåværendeArbeidsgiver": {
                "merEnn4Uker": true,
                "begrunnelse": "ANNET_ARBEIDSFORHOLD"
            },
            "arbeidsgivere": {
              "organisasjoner": [
                {
                    "navn": "Arbeidsgiver 1",
                    "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false,
                    "perioder": [
                      {
                        "fraOgMed": "2020-01-01",
                        "tilOgMed": "2020-01-11",
                        "lengde": null
                      }
                    ]
                },
                {
                  "navn": "Arbeidsgiver 2",
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false,
                    "perioder": [
                      {
                        "fraOgMed": "2020-01-21",
                        "tilOgMed": "2020-01-21",
                        "lengde": "PT5H30M"
                      }
                    ]
                },
                {
                  "navn": "Arbeidsgiver 3",
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false,
                    "perioder": [
                      {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-05",
                        "lengde": null
                      }
                    ]
                },
                {
                  "navn": null,
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false,
                    "perioder": [
                      {
                        "fraOgMed": "2020-02-01",
                        "tilOgMed": "2020-02-06",
                        "lengde": null
                      }
                    ]
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
            "andreUtbetalinger": ["dagpenger", "sykepenger"],
            "fosterbarn": [{
                "fødselsnummer": "02119970078"
            }],
            "vedlegg": []
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
            "jobbHosNåværendeArbeidsgiver": {
                "merEnn4Uker": true,
                "begrunnelse": "ANNET_ARBEIDSFORHOLD"
            },
            "arbeidsgivere": {
              "organisasjoner": [
                {
                    "navn": "Arbeidsgiver 1",
                    "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false,
                    "perioder": [
                      {
                        "fraOgMed": "2020-01-01",
                        "tilOgMed": "2020-01-11",
                        "lengde": null
                      }
                    ]
                },
                {
                  "navn": "Arbeidsgiver 2",
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false,
                    "perioder": [
                      {
                        "fraOgMed": "2020-01-21",
                        "tilOgMed": "2020-01-21",
                        "lengde": "PT5H30M"
                      }
                    ]
                },
                {
                  "navn": "Arbeidsgiver 3",
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false,
                    "perioder": [
                      {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-05",
                        "lengde": null
                      }
                    ]
                },
                {
                  "navn": null,
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false,
                    "perioder": [
                      {
                        "fraOgMed": "2020-02-01",
                        "tilOgMed": "2020-02-06",
                        "lengde": null
                      }
                    ]
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
            "andreUtbetalinger": ["dagpenger", "sykepenger"],
            "fosterbarn": [{
                "fødselsnummer": "02119970078"
            }],
            "vedlegg": []
        }
        """.trimIndent()
    }
}
