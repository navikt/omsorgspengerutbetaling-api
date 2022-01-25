package no.nav.omsorgspengerutbetaling.validering

import no.nav.omsorgspengerutbetaling.SøknadUtils
import no.nav.omsorgspengerutbetaling.TestUtils.Companion.validerOgAssertMangler
import no.nav.omsorgspengerutbetaling.soknad.Bosted
import no.nav.omsorgspengerutbetaling.soknad.JaNei
import java.time.LocalDate
import kotlin.test.Test


class OppholdOgBostedValideringTest {
    val gyldigBosted = Bosted(
        fraOgMed = LocalDate.parse("2021-01-01"),
        tilOgMed = LocalDate.parse("2021-01-01"),
        landkode = "GBR",
        landnavn = "Great Britain",
        erEØSLand = JaNei.Ja
    )
    val gyldigSøknad = SøknadUtils.hentGyldigSøknad().copy(
        bosteder = listOf(gyldigBosted),
        opphold = listOf()
    )

    @Test
    fun `Gyldig søknad gir ingen feil`(){
        validerOgAssertMangler(gyldigSøknad, false)
    }

    @Test
    fun `Skal gi feil dersom fraOgMed er før tilOgMed`(){
        val søknad = gyldigSøknad.copy(
            bosteder = listOf(
                gyldigBosted.copy(
                    fraOgMed = LocalDate.parse("2021-01-02"),
                    tilOgMed = LocalDate.parse("2021-01-01")
                )
            )
        )

        val forventetMangler = """
            [{
              "reason": "Til og med må være etter eller lik fra og med",
              "name": "bosteder[0]",
              "invalid_value": "fraOgMed=2021-01-02, tilOgMed=2021-01-01",
              "type": "entity"
            }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    fun `Skal gi feil dersom landnavn og landkode er blank`(){
        val søknad = gyldigSøknad.copy(
            bosteder = listOf(
                gyldigBosted.copy(
                    landnavn = " ",
                    landkode = " "
                )
            )
        )

        val forventetMangler = """
            [{
              "reason": "Landkode er ikke en gyldig ISO 3166-1 alpha-3 kode.",
              "name": "bosteder[0].landkode",
              "invalid_value": " ",
              "type": "entity"
            }, {
              "reason": "Landkode kan ikke være blank.",
              "name": "bosteder[0].landkode",
              "invalid_value": " ",
              "type": "entity"
            }, {
              "reason": "Landnavn kan ikke være blank.",
              "name": "bosteder[0].landnavn",
              "invalid_value": " ",
              "type": "entity"
            }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }
}