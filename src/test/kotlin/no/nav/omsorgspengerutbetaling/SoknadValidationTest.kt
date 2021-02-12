package no.nav.omsorgspengerutbetaling

import no.nav.omsorgspengerutbetaling.k9format.tilK9SelvstendingNæringsdrivendeInfo
import no.nav.omsorgspengerutbetaling.soknad.*
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SøknadValideringsTest {

    @Test
    internal fun `Virksomhet er nyoppstartet dersom den er yngre enn 3 år`() {
        val k9SelvstendingNæringsdrivendeInfo = Virksomhet(
            næringstyper = listOf(
                Næringstyper.JORDBRUK_SKOGBRUK,
                Næringstyper.FISKE,
                Næringstyper.DAGMAMMA,
                Næringstyper.ANNEN
            ),
            fraOgMed = LocalDate.parse("2021-01-01"),
            tilOgMed = LocalDate.parse("2021-01-10"),
            næringsinntekt = 123123,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = JaNei.Ja,
            organisasjonsnummer = "101010",
            erNyoppstartet = true
        ).tilK9SelvstendingNæringsdrivendeInfo()

        assertTrue { k9SelvstendingNæringsdrivendeInfo.erNyoppstartet }
    }

    @Test
    internal fun `Virksomhet er ikke nyoppstartet dersom den er eldre enn 3 år`() {
        val k9SelvstendingNæringsdrivendeInfo = Virksomhet(
            næringstyper = listOf(
                Næringstyper.JORDBRUK_SKOGBRUK,
                Næringstyper.FISKE,
                Næringstyper.DAGMAMMA,
                Næringstyper.ANNEN
            ),
            fraOgMed = LocalDate.parse("2017-01-01"),
            tilOgMed = LocalDate.parse("2021-01-10"),
            næringsinntekt = 123123,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = JaNei.Ja,
            organisasjonsnummer = "101010",
            erNyoppstartet = true
        ).tilK9SelvstendingNæringsdrivendeInfo()

        assertFalse { k9SelvstendingNæringsdrivendeInfo.erNyoppstartet }
    }

    @Test
    internal fun `Virksomhet er ikke nyoppstartet dersom den er akkurat 3 år`() {
        val k9SelvstendingNæringsdrivendeInfo = Virksomhet(
            næringstyper = listOf(
                Næringstyper.JORDBRUK_SKOGBRUK,
                Næringstyper.FISKE,
                Næringstyper.DAGMAMMA,
                Næringstyper.ANNEN
            ),
            fraOgMed = LocalDate.now().minusYears(3),
            tilOgMed = LocalDate.parse("2021-01-10"),
            næringsinntekt = 123123,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = JaNei.Ja,
            organisasjonsnummer = "101010",
            erNyoppstartet = true
        ).tilK9SelvstendingNæringsdrivendeInfo()

        assertFalse { k9SelvstendingNæringsdrivendeInfo.erNyoppstartet }
    }

    // TODO: Legge til valideringstester fremfor flere tester i ApplicationTest

    fun forLangtNavn() =
        "DetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangtDetteNavnetErForLangt"

}
