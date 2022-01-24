package no.nav.omsorgspengerutbetaling.validering

import no.nav.omsorgspengerutbetaling.SøknadUtils
import no.nav.omsorgspengerutbetaling.TestUtils.Companion.validerOgAssertMangler
import no.nav.omsorgspengerutbetaling.soknad.Frilans
import no.nav.omsorgspengerutbetaling.soknad.JaNei
import java.time.LocalDate
import kotlin.test.Test

class FrilansValideringTest {
    val gyldigSøknad = SøknadUtils.hentGyldigSøknad()

    @Test
    fun `Gyldig søknad gir ingen feil`(){
        validerOgAssertMangler(gyldigSøknad, false)
    }

    @Test
    fun `Frilans uten sluttdato og jobber ikke lenger gir feil`(){
        val søknad = gyldigSøknad.copy(
            frilans = Frilans(
                startdato = LocalDate.parse("2021-01-02"),
                sluttdato = null,
                jobberFortsattSomFrilans = JaNei.Nei
            )
        )

        val forventetMangler = """
            [{
              "reason": "Sluttdato kan ikke være null dersom jobberFortsattSomFrilans=Nei",
              "name": "frilans.sluttdato",
              "invalid_value": "sluttdato=null,jobberFortsattSomFrilans=Nei ",
              "type": "entity"
            }]
        """.trimIndent()

        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    fun `Frilans med sluttdato før startdato gir feil`(){
        val søknad = gyldigSøknad.copy(
            frilans = Frilans(
                startdato = LocalDate.parse("2021-01-02"),
                sluttdato = LocalDate.parse("2021-01-01"),
                jobberFortsattSomFrilans = JaNei.Ja
            )
        )

        val forventetMangler = """
            [{
              "reason": "Sluttdato kan ikke være før startdato",
              "name": "frilans.sluttdato",
              "invalid_value": "startdato=2021-01-02, sluttdato=2021-01-01",
              "type": "entity"
            }]
        """.trimIndent()

        validerOgAssertMangler(søknad, true, forventetMangler)
    }
}

