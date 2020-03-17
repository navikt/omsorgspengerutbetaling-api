package no.nav.omsorgspengerutbetaling

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.omsorgspengerutbetaling.soknad.*
import org.junit.Test
import java.net.URL
import java.time.LocalDate
import kotlin.test.assertTrue


internal class SøknadValideringsTest {

    companion object {
        private val gyldigFodselsnummerA = "02119970078"
        private val gyldigFodselsnummerB = "19066672169"
        private val gyldigFodselsnummerC = "20037473937"
        private val dNummerA = "55125314561"
    }

    // Test for lange navn..

    fun forLangtNavn() =
        "DetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangt"

}
