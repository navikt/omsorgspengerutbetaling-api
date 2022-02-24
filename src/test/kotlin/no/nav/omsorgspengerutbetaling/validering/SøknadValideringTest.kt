package no.nav.omsorgspengerutbetaling.validering

import no.nav.omsorgspengerutbetaling.SøknadUtils
import no.nav.omsorgspengerutbetaling.TestUtils.Companion.validerOgAssertMangler
import no.nav.omsorgspengerutbetaling.soknad.*
import java.time.LocalDate
import kotlin.test.Test

class SøknadValideringTest {
    val gyldigSøknad = SøknadUtils.hentGyldigSøknad()

    @Test
    fun `Gyldig søknad gir ingen feil`(){
        validerOgAssertMangler(gyldigSøknad, false)
    }

    @Test
    fun `Hvis et barn er 12 år skal det gi feil dersom harDekketTiFørsteDagerSelv ikke er true`(){
        val søknad = gyldigSøknad.copy(
            harDekketTiFørsteDagerSelv = null,
            barn = listOf(
                Barn(
                    navn = "Barn Barnesen",
                    type = TypeBarn.FOSTERBARN,
                    fødselsdato = LocalDate.now().minusYears(11),
                    aktørId = "1000000000001",
                    identitetsnummer = "16012099359"
                )
            )
        )
        val forventetMangler = """
            [{
                "reason": "harDekketTiFørsteDagerSelv må være true dersom et barn er 12 år eller yngre.",
                "name": "harDekketTiFørsteDagerSelv",
                "invalid_value": null,
                "type": "entity"
            }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    fun `Skal feile dersom alle barn er 13 år og ingen har utvidet rett`(){
        val søknad = gyldigSøknad.copy(
            barn = listOf(
                Barn(
                    navn = "Barn Barnesen",
                    utvidetRett = false,
                    fødselsdato = LocalDate.now().minusYears(13),
                    identitetsnummer = "16012099359",
                    type = TypeBarn.FOSTERBARN
                ),
                Barn(
                    navn = "Barn Barnesen",
                    utvidetRett = false,
                    fødselsdato = LocalDate.now().minusYears(15),
                    identitetsnummer = "16012099359",
                    type = TypeBarn.FOSTERBARN
                )
            )
        )

        val forventetMangler = """
            [{
              "reason": "Hvis alle barn er 13 år eller eldre må minst et barn ha utvidet rett",
              "name": "barn[?].utvidetRett",
              "invalid_value": null,
              "type": "entity"
            }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }


    @Test
    fun `Spørsmål kan ikke være tom`(){
        val søknad = gyldigSøknad.copy(
            spørsmål = listOf(
                SpørsmålOgSvar(
                    spørsmål = "",
                    svar = JaNei.Ja
                )
            )
        )
        val forventetMangler = """
            [{
              "reason": "Spørsmål må være satt og være maks 1000 tegn.",
              "name": "spørsmål[0].spørsmål",
              "invalid_value": "",
              "type": "entity"
            }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    fun `Gir feil dersom Bekreftelser er NEI`() {
        val søknad = gyldigSøknad.copy(
            bekreftelser = Bekreftelser(
                harBekreftetOpplysninger = JaNei.Nei, harForståttRettigheterOgPlikter = JaNei.Nei
            )
        )
        val forventetMangler = """
            [{
              "reason": "Må besvars Ja.",
              "name": "bekreftlser.harBekreftetOpplysninger",
              "invalid_value": false,
              "type": "entity"
            }, {
              "reason": "Må besvars Ja.",
              "name": "bekreftelser.harForståttRettigheterOgPlikter",
              "invalid_value": false,
              "type": "entity"
            }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    fun `Gir feil dersom verken frilans eller snf er satt`() {
        val søknad = gyldigSøknad.copy(
            frilans = null,
            selvstendigNæringsdrivende = null
        )
        val forventetMangler = """
            [{
              "reason": "Må settes 'frilans' eller 'selvstendigNæringsdrivende'",
              "name": "frilans/selvstendigNæringsdrivende",
              "invalid_value": null,
              "type": "entity"
            }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    fun `Gir feil dersom barn eller fosterbarn har ugyldig fødselsnummer eller identitetsnummer`() {
        val søknad = gyldigSøknad.copy(
            barn = listOf(
                Barn(
                    navn = "Barn Barnesen",
                    fødselsdato = LocalDate.now().minusYears(11),
                    identitetsnummer = "UGYLDIG",
                    type = TypeBarn.FOSTERBARN
                ),
                Barn(
                    navn = "Barn Barnesen",
                    fødselsdato = LocalDate.now().minusYears(11),
                    identitetsnummer = null,
                    type = TypeBarn.FOSTERBARN
                )
            )
        )
        val forventetMangler = """
            [{
              "reason": "Ugyldig identitetsnummer",
              "name": "barn[0].identitetsnummer",
              "invalid_value": null,
              "type": "entity"
            }, {
              "reason": "identitetsnummer må være satt",
              "name": "barn[1].identitetsnummer",
              "invalid_value": null,
              "type": "entity"
            }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

}