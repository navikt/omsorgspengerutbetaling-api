package no.nav.omsorgspengerutbetaling

class SoknadUtils {
    companion object {
        fun forLangtNavn() =
            "DetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangt"

        fun bodyMedFodselsnummerPaaBarn(
            fodselsnummer: String,
            legeerklæringUrl: String,
            samværsavtaleUrl: String,
            relasjonTilBarnet: String? = "mor"
        ): String {
            //language=JSON
            return """
                {
                  "nyVersjon": true,
                  "språk": "nb",
                  "arbeidssituasjon": ["Arbeidstaker", "Frilans", "Selvstendig Næringsdrivende"],
                  "kroniskEllerFunksjonshemming": true,
                  "barn": {
                    "navn": "Ole Dole Doffen",
                    "fødselsdato": "2005-02-23",
                    "fødselsnummer": "$fodselsnummer",
                    "aktørId": "123456"
                  },
                  "sammeAddresse": true,
                  "relasjonTilBarnet": "$relasjonTilBarnet",
                  "legeerklæring": [
                    "$legeerklæringUrl"
                  ],
                  "samværsavtale": [
                    "$samværsavtaleUrl"
                  ],
                  "medlemskap": {
                    "harBoddIUtlandetSiste12Mnd": true,
                    "utenlandsoppholdSiste12Mnd": [
                      {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-31",
                        "landkode": "DK",
                        "landnavn": "Danmark"
                      }
                    ],
                    "skalBoIUtlandetNeste12Mnd": true,
                    "utenlandsoppholdNeste12Mnd": [
                      {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-31",
                        "landkode": "DK",
                        "landnavn": "Danmark"
                      }
                    ]
                  },
                  "harForståttRettigheterOgPlikter": true,
                  "harBekreftetOpplysninger": true
                }
            """.trimIndent()
        }

        fun bodyMedAktoerIdPaaBarn(
            aktoerId: String,
            legeerklæringUrl: String,
            samværsavtaleUrl: String,
            barnetsNorskIdentifikator: String?
        ): String {
            //language=JSON
            return """
                {
                  "nyVersjon": true,
                  "språk": "nb",
                  "arbeidssituasjon": ["Arbeidstaker", "Frilans", "Selvstendig Næringsdrivende"],
                  "kroniskEllerFunksjonshemming": true,
                  "barn": {
                    "fødselsdato": "2005-02-23",
                    "aktørId": "$aktoerId"
                  },
                  "sammeAddresse": true,
                  "relasjonTilBarnet": "mor",
                  "legeerklæring": [
                    "$legeerklæringUrl"
                  ],
                  "samværsavtale": [
                    "$samværsavtaleUrl"
                  ],
                  "medlemskap": {
                    "harBoddIUtlandetSiste12Mnd": true,
                    "utenlandsoppholdSiste12Mnd": [
                      {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-31",
                        "landkode": "DK",
                        "landnavn": "Danmark"
                      }
                    ],
                    "skalBoIUtlandetNeste12Mnd": true,
                    "utenlandsoppholdNeste12Mnd": [
                      {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-31",
                        "landkode": "DK",
                        "landnavn": "Danmark"
                      }
                    ]
                  },
                  "harForståttRettigheterOgPlikter": true,
                  "harBekreftetOpplysninger": true
                }
            """.trimIndent()
        }


        fun bodyUtenIdPaaBarn(
            legeerklæringUrl: String,
            samværsavtaleUrl: String
        ): String {
            //language=JSON
            return """
                {
                  "nyVersjon": true,
                  "språk": "nb",
                  "arbeidssituasjon": ["Arbeidstaker", "Frilans", "Selvstendig Næringsdrivende"],
                  "kroniskEllerFunksjonshemming": true,
                  "barn": {
                    "fødselsdato": "2005-01-23"
                  },
                  "sammeAddresse": true,
                  "relasjonTilBarnet": "mor",
                  "legeerklæring": [
                    "$legeerklæringUrl"
                  ],
                  "samværsavtale": [
                    "$samværsavtaleUrl"
                  ],
                  "medlemskap": {
                    "harBoddIUtlandetSiste12Mnd": true,
                    "utenlandsoppholdSiste12Mnd": [
                      {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-31",
                        "landkode": "DK",
                        "landnavn": "Danmark"
                      }
                    ],
                    "skalBoIUtlandetNeste12Mnd": true,
                    "utenlandsoppholdNeste12Mnd": [
                      {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-31",
                        "landkode": "DK",
                        "landnavn": "Danmark"
                      }
                    ]
                  },
                  "harForståttRettigheterOgPlikter": true,
                  "harBekreftetOpplysninger": true
                }
            """.trimIndent()
        }
    }
}
