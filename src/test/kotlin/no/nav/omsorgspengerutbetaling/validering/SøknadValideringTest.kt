package no.nav.omsorgspengerutbetaling.validering

import no.nav.omsorgspengerutbetaling.SøknadUtils
import no.nav.omsorgspengerutbetaling.TestUtils
import no.nav.omsorgspengerutbetaling.soknad.Barn
import java.time.LocalDate
import kotlin.test.Test

class SøknadValideringTest {
    val gyldigSøknad = SøknadUtils.hentGyldigSøknad()

    @Test
    fun `Gyldig søknad gir ingen feil`(){
        TestUtils.validerOgAssertMangler(gyldigSøknad, false)
    }

    @Test
    fun `Hvis et barn er 12 år skal det gi feil dersom harDekketTiFørsteDagerSelv ikke er true`(){
        val søknad = gyldigSøknad.copy(
            harDekketTiFørsteDagerSelv = null,
            barn = listOf(
                Barn(
                    navn = "Barn Barnesen",
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
        TestUtils.validerOgAssertMangler(søknad, true, forventetMangler)
    }

}