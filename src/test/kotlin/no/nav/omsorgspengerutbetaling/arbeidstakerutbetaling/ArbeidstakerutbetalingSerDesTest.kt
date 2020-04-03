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

        internal val søknad = ArbeidstakerutbetalingSøknadUtils.defaultSøknad.copy(
            utbetalingsperioder = listOf(
                Utbetalingsperiode(
                    fraOgMed = start,
                    tilOgMed = start.plusDays(10)
                ),
                Utbetalingsperiode(
                    fraOgMed = start.plusDays(20),
                    tilOgMed = start.plusDays(20),
                    lengde = Duration.ofHours(5).plusMinutes(30)
                ),
                Utbetalingsperiode(
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
                    "arbeidsgiverHarUtbetaltLønn": false
                },
                {
                  "navn": "Arbeidsgiver 2",
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false
                },
                {
                  "navn": "Arbeidsgiver 3",
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false
                },
                {
                  "navn": null,
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false
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
            "andreUtbetalinger": ["dagpenger", "sykepenger"],
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
                    "arbeidsgiverHarUtbetaltLønn": false
                },
                {
                  "navn": "Arbeidsgiver 2",
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false
                },
                {
                  "navn": "Arbeidsgiver 3",
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false
                },
                {
                  "navn": null,
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false
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
            "andreUtbetalinger": ["dagpenger", "sykepenger"],
            "fosterbarn": [{
                "fødselsnummer": "02119970078",
                "fornavn": "fornavn",
                "etternavn": "etternavn"
            }]
        }
        """.trimIndent()
    }
}
