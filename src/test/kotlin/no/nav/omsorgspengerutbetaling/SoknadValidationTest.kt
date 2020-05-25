package no.nav.omsorgspengerutbetaling

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.omsorgspengerutbetaling.soknad.Endring
import no.nav.omsorgspengerutbetaling.soknad.EndringArbeidssituasjon
import no.nav.omsorgspengerutbetaling.soknad.JaNei
import no.nav.omsorgspengerutbetaling.soknad.valider
import org.junit.Test
import java.time.LocalDate

internal class SøknadValideringsTest {

    companion object {
        private val gyldigFodselsnummerA = "02119970078"
        private val gyldigFodselsnummerB = "19066672169"
        private val gyldigFodselsnummerC = "20037473937"
        private val dNummerA = "55125314561"
    }

    // TODO: Legge til valideringstester fremfor flere tester i ApplicationTest

    fun forLangtNavn() = "DetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangt"



    @Test(expected = Throwblem::class)
    fun `Skal feile dersom jaNeiFrilans satt til true men det ikke er lagt inn noen endringer for frilans`() {
        val søknad = SøknadUtils.defaultSøknad.copy(
            endringArbeidssituasjon = EndringArbeidssituasjon(
                harEndringFrilans = JaNei.Ja,
                harEndringSelvstendig = JaNei.Nei
            )
        )
        søknad.valider()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom jaNeiSelvstendig er satt til true men det ikke er lagt inn noen endringer for selvstendig`() {
        val søknad = SøknadUtils.defaultSøknad.copy(
            endringArbeidssituasjon = EndringArbeidssituasjon(
                harEndringFrilans = JaNei.Nei,
                harEndringSelvstendig = JaNei.Ja
            )
        )
        søknad.valider()
    }

    @Test
    fun `Skal ikke feile dersom endringArbeidssituasjon er satt korrekt`() {
        val søknad = SøknadUtils.defaultSøknad.copy(
            endringArbeidssituasjon = EndringArbeidssituasjon(
                harEndringFrilans = JaNei.Ja,
                endringerFrilans = listOf(
                    Endring(
                        dato = LocalDate.now(),
                        forklaring = "Forklaring endring"
                    )
                ),
                harEndringSelvstendig = JaNei.Ja,
                endringerSelvstendig = listOf(
                    Endring(
                        dato = LocalDate.now(),
                        forklaring = "Forklaring endring"
                    )
                )
            )
        )
        søknad.valider()
    }

    @Test
    fun `Skal ikke feile dersom endringArbeidssituasjon er null`() {
        val søknad = SøknadUtils.defaultSøknad.copy(
            endringArbeidssituasjon = null
        )
        søknad.valider()
    }
}
