package no.nav.omsorgspengerutbetaling

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.omsorgspengerutbetaling.soknad.*
import org.skyscreamer.jsonassert.JSONAssert
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SerDesTest {

    @Test
    internal fun `Test reserialisering av request`() {
        val søknadId = UUID.randomUUID().toString()

        val søknad = søknad.copy(søknadId = SøknadId(søknadId))
        val søknadJson = søknadJson(søknadId)

        JSONAssert.assertEquals(søknadJson, søknad.somJson(), true)
        assertEquals(søknad, SøknadUtils.objectMapper.readValue(søknadJson))
    }

    @Test
    fun `Test serialisering av request til prosessering`() {
        val søknadId = UUID.randomUUID().toString()

        val komplettSoknad = SøknadUtils.defaultKomplettSøknad(SøknadId(søknadId))
        val komplettSøknadJson = komplettSøknadJson(søknadId)

        JSONAssert.assertEquals(komplettSøknadJson, komplettSoknad.somJson(), true)
        //assertEquals(komplettSoknad, SøknadUtils.objectMapper.readValue(komplettSøknadJson)) // TODO: 05/02/2021  Må fikses før prodsetting.
    }

    private companion object {
        val start = LocalDate.parse("2020-01-01")

        val søknad = SøknadUtils.defaultSøknad.copy(
            utbetalingsperioder = listOf(
                Utbetalingsperiode(
                    fraOgMed = start,
                    tilOgMed = start.plusDays(10),
                    årsak = FraværÅrsak.STENGT_SKOLE_ELLER_BARNEHAGE,
                    aktivitetFravær = listOf(AktivitetFravær.FRILANSER)
                ),
                Utbetalingsperiode(
                    fraOgMed = start.plusDays(20),
                    tilOgMed = start.plusDays(20),
                    antallTimerPlanlagt = Duration.ofHours(7).plusMinutes(30),
                    antallTimerBorte = Duration.ofHours(7).plusMinutes(30),
                    årsak = FraværÅrsak.SMITTEVERNHENSYN,
                    aktivitetFravær = listOf(AktivitetFravær.SELVSTENDIG_VIRKSOMHET)
                ),
                Utbetalingsperiode(
                    fraOgMed = start.plusDays(30),
                    tilOgMed = start.plusDays(35),
                    årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                    aktivitetFravær = listOf(AktivitetFravær.FRILANSER, AktivitetFravær.SELVSTENDIG_VIRKSOMHET)
                )
            ),
            frilans = Frilans(
                startdato = start,
                sluttdato = null,
                jobberFortsattSomFrilans = JaNei.Ja
            ),
            selvstendigVirksomheter = listOf(
                SelvstendigNæringsdrivende(
                    harFlereAktiveVirksomheter = true,
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
                    registrertIUtlandet = Land(
                        landkode = "DEU",
                        landnavn = "Tyskland"
                    ),
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
                    erNyoppstartet = true
                )
            ),
            vedlegg = listOf(
                URL("http://localhost:8080/vedlegg/1"),
                URL("http://localhost:8080/vedlegg/2"),
                URL("http://localhost:8080/vedlegg/3")
            )
        )

        //language=json
        fun komplettSøknadJson(søknadId: String) = """
        {
            "søknadId": "$søknadId",
            "mottatt": "2018-01-02T03:04:05.000000006Z",
            "språk": "nb",
            "harDekketTiFørsteDagerSelv": true,
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
                "antallTimerBorte": "PT3H",
                "antallTimerPlanlagt": "PT5H",
                "årsak": "STENGT_SKOLE_ELLER_BARNEHAGE",
                "aktivitetFravær": ["FRILANSER"]
            }, {
                "fraOgMed": "2020-01-21",
                "tilOgMed": "2020-01-21",
                "antallTimerBorte": "PT3H",
                "antallTimerPlanlagt": "PT5H",
                "årsak": "SMITTEVERNHENSYN",
                "aktivitetFravær": ["SELVSTENDIG_VIRKSOMHET"]
            }, {
                "fraOgMed": "2020-01-31",
                "tilOgMed": "2020-02-05",
                "antallTimerBorte": "PT3H",
                "antallTimerPlanlagt": "PT5H",
                "årsak": "ORDINÆRT_FRAVÆR",
                "aktivitetFravær": ["FRILANSER", "SELVSTENDIG_VIRKSOMHET"]
            }],
            "andreUtbetalinger": ["dagpenger", "sykepenger"],
            "frilans": {
                "startdato": "2020-01-01",
                "sluttdato": null,
                "jobberFortsattSomFrilans": true
            },
            "selvstendigNæringsdrivende" : null,
            "selvstendigVirksomheter": [{
                "harFlereAktiveVirksomheter": true,
                "næringstyper": ["JORDBRUK_SKOGBRUK", "FISKE", "DAGMAMMA", "ANNEN"],
                "fiskerErPåBladB": false,
                "fraOgMed": "2019-12-31",
                "tilOgMed": "2020-01-01",
                "næringsinntekt": 123123,
                "navnPåVirksomheten": "TullOgTøys",
                "organisasjonsnummer": "916974574",
                "registrertINorge": false,
                "erNyoppstartet": true,
                "registrertIUtlandet": {
                  "landkode": "DEU",
                  "landnavn": "Tyskland"
                },
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
                }
            }],
            "erArbeidstakerOgså": true,
            "fosterbarn": [{
                "fødselsnummer": "02119970078"
            }],
            "vedlegg": [],
            "k9FormatSøknad": {
                "søknadId": "$søknadId",
                "mottattDato": "2018-01-02T03:04:05.000Z",
                "versjon": "1.0.0",
                "språk": "nb",
                "søker": {
                    "norskIdentitetsnummer": "02119970078"
                },
                "ytelse": {
                    "type": "OMP_UT",
                    "fosterbarn": [
                      {
                        "norskIdentitetsnummer": "02119970078",
                        "fødselsdato": null
                      }
                    ],
                    "aktivitet": {
                      "selvstendigNæringsdrivende": [
                        {
                          "perioder": {
                            "2019-12-31/2020-01-01": {
                              "virksomhetstyper": [
                                "JORDBRUK_SKOGBRUK",
                                "FISKE",
                                "DAGMAMMA",
                                "ANNEN"
                              ],
                              "regnskapsførerNavn": "Kjell",
                              "regnskapsførerTlf": "84554",
                              "erVarigEndring": true,
                              "endringDato": "2020-01-01",
                              "endringBegrunnelse": "Fordi",
                              "bruttoInntekt": 1337,
                              "erNyoppstartet": true,
                              "erNyIArbeidslivet": true,
                              "registrertIUtlandet": true,
                              "landkode": "DEU"
                            }
                          },
                          "organisasjonsnummer": "916974574",
                          "virksomhetNavn": "TullOgTøys"
                        }
                      ],
                      "frilanser": {
                        "startdato": "2020-01-01",
                        "sluttdato": null
                      }
                    },
                    "fraværsperioder": [
                      {
                        "periode": "2020-01-01/2020-01-11",
                        "duration": "PT3H", 
                        "årsak": "STENGT_SKOLE_ELLER_BARNEHAGE",
                        "aktivitetFravær": ["FRILANSER"],
                        "arbeidsgiverOrgNr": null,
                        "søknadÅrsak": null
                      },
                      {
                        "periode": "2020-01-21/2020-01-21",
                        "duration": "PT3H", 
                        "årsak": "SMITTEVERNHENSYN",
                        "aktivitetFravær": ["SELVSTENDIG_VIRKSOMHET"],
                        "arbeidsgiverOrgNr": null,
                        "søknadÅrsak": null
                      },
                      {
                        "periode": "2020-01-31/2020-02-05",
                        "duration": "PT3H", 
                        "årsak": "ORDINÆRT_FRAVÆR",
                        "aktivitetFravær": ["FRILANSER", "SELVSTENDIG_VIRKSOMHET"],
                        "arbeidsgiverOrgNr": null,
                        "søknadÅrsak": null
                      }
                    ],
                    "bosteder": {
                      "perioder": {
                        "2019-12-12/2019-12-22": {
                          "land": "GB"
                        }
                      },
                      "perioderSomSkalSlettes": {}
                    },
                    "utenlandsopphold": {
                      "perioder": {
                        "2019-12-12/2019-12-22": {
                          "land": "GB",
                          "årsak": null
                        }
                      },
                      "perioderSomSkalSlettes": {}
                    }
                  },
              "journalposter": []
            }
        }
        """.trimIndent()

        //language=json
        fun søknadJson(søknadId: String) = """
        {
            "søknadId": "$søknadId",
            "språk": "nb",
            "harDekketTiFørsteDagerSelv": true,
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
                "antallTimerBorte": null,
                "antallTimerPlanlagt": null,
                "årsak": "STENGT_SKOLE_ELLER_BARNEHAGE",
                "aktivitetFravær": ["FRILANSER"]
            }, {
                "fraOgMed": "2020-01-21",
                "tilOgMed": "2020-01-21",
                "antallTimerBorte": "PT7H30M",
                "antallTimerPlanlagt": "PT7H30M",
                "årsak": "SMITTEVERNHENSYN",
                "aktivitetFravær": ["SELVSTENDIG_VIRKSOMHET"]
            }, {
                "fraOgMed": "2020-01-31",
                "tilOgMed": "2020-02-05",
                "antallTimerBorte": null,
                "antallTimerPlanlagt": null,
                "årsak": "ORDINÆRT_FRAVÆR",
                "aktivitetFravær": ["FRILANSER", "SELVSTENDIG_VIRKSOMHET"]
            }],
            "andreUtbetalinger": ["dagpenger", "sykepenger", "midlertidigkompensasjonsnfri"],
            "frilans": {
                "startdato": "2020-01-01",
                "sluttdato": null,
                "jobberFortsattSomFrilans": true
            },
            "selvstendigNæringsdrivende" : null,
            "selvstendigVirksomheter": [{
                "harFlereAktiveVirksomheter": true,
                "næringstyper": ["JORDBRUK_SKOGBRUK", "FISKE", "DAGMAMMA", "ANNEN"],
                "fiskerErPåBladB": true,
                "fraOgMed": "2020-01-01",
                "tilOgMed": "2020-01-11",
                "næringsinntekt": 100000,
                "navnPåVirksomheten": "Test",
                "organisasjonsnummer": "111",
                "registrertINorge": false,
                "erNyoppstartet": true,
                "registrertIUtlandet": {
                  "landkode": "DEU",
                  "landnavn": "Tyskland"
                },
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
                }
            }],
            "erArbeidstakerOgså": true,
            "fosterbarn": [{
                "fødselsnummer": "02119970078"
            }],
            "vedlegg": [
              "http://localhost:8080/vedlegg/1",
              "http://localhost:8080/vedlegg/2",
              "http://localhost:8080/vedlegg/3"
            ]
        }
        """.trimIndent()


    }
}
