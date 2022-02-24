package no.nav.omsorgspengerutbetaling.k9format

import no.nav.omsorgspengerutbetaling.SøknadUtils
import no.nav.omsorgspengerutbetaling.felles.somJson
import no.nav.omsorgspengerutbetaling.soknad.Barn
import no.nav.omsorgspengerutbetaling.soknad.TypeBarn
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.Test

class K9FormatTest {

    @Test
    fun `Mapper kun opp fosterbarn til k9 format`(){
        val barn = listOf(
            Barn(
                navn = "Barn1",
                type = TypeBarn.FOSTERBARN,
                fødselsdato = LocalDate.parse("2021-01-01"),
                identitetsnummer = "1"
            ),
            Barn(
                navn = "Barn3",
                type = TypeBarn.ANNET,
                fødselsdato = LocalDate.parse("2021-01-01"),
                identitetsnummer = "3"
            ),
            Barn(
                navn = "Barn4",
                type = TypeBarn.FRA_OPPSLAG,
                fødselsdato = LocalDate.parse("2021-01-01"),
                identitetsnummer = "4"
            ),
            Barn(
                navn = "Barn5",
                type = TypeBarn.FOSTERBARN,
                fødselsdato = LocalDate.parse("2021-01-01"),
                identitetsnummer = "5"
            ),
        )
        val søknad = SøknadUtils.hentGyldigSøknad().copy(
            barn = barn,
            fosterbarn = listOf()
        )
        val k9Barn = søknad.byggK9Barn()
        val forventetK9Barn = """
            [
              {
                "norskIdentitetsnummer": "1",
                "fødselsdato": null
              },
              {
                "norskIdentitetsnummer": "5",
                "fødselsdato": null
              }
            ]
        """.trimIndent()

        JSONAssert.assertEquals(forventetK9Barn, k9Barn.somJson(), true)
    }
}