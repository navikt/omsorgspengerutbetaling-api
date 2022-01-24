package no.nav.omsorgspengerutbetaling.validering

import no.nav.omsorgspengerutbetaling.SøknadUtils
import no.nav.omsorgspengerutbetaling.soknad.Barn
import no.nav.omsorgspengerutbetaling.soknad.valider
import java.time.LocalDate
import kotlin.test.Test

class ValideringTest {
    val gyldigSøknad = SøknadUtils.hentGyldigSøknad().copy(
        barn = listOf(
            Barn(
                navn = "Barn Barnesen",
                fødselsdato = LocalDate.parse("2021-01-01"),
                aktørId = "1000000000001",
                identitetsnummer = "16012099359"
            )
        )
    )

    @Test
    fun `Gyldig søknad gir ingen feil`(){
        gyldigSøknad.valider()
    }

}