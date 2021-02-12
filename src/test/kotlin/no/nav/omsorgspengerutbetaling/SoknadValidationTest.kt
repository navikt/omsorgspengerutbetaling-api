package no.nav.omsorgspengerutbetaling

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgspengerutbetaling.soknad.JaNei
import no.nav.omsorgspengerutbetaling.soknad.Næringstyper
import no.nav.omsorgspengerutbetaling.soknad.Virksomhet
import no.nav.omsorgspengerutbetaling.soknad.validate
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

internal class SøknadValideringsTest {

    @Test
    internal fun `Virksomhet anses ikke som nyoppstartet når fraOgMed er mer enn 4 år siden`() {
        val fraOgMed = LocalDate.now().minusYears(4).minusDays(1)
        val feil: MutableSet<Violation> = Virksomhet(
            næringstyper = listOf(
                Næringstyper.JORDBRUK_SKOGBRUK,
                Næringstyper.FISKE,
                Næringstyper.DAGMAMMA,
                Næringstyper.ANNEN
            ),
            fraOgMed = fraOgMed,
            tilOgMed = LocalDate.parse("2021-01-10"),
            næringsinntekt = 123123,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = JaNei.Ja,
            organisasjonsnummer = "101010",
            erNyoppstartet = true
        ).validate(0)

        assertEquals(feil.size, 1)
        assertEquals(
            feil.first(),
            Violation(
                parameterName = "selvstendigVirksomheter[0].erNyoppstartet",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis erNyoppstartet er true så må fraOgMed være etter ${LocalDate.now().minusYears(4)}",
                invalidValue = fraOgMed
            )
        )
    }

    @Test
    internal fun `Virksomhet anses ikke som nyoppstartet når fraOgMed er akkurat 4 år siden`() {
        val fraOgMed = LocalDate.now().minusYears(4)
        val feil: MutableSet<Violation> = Virksomhet(
            næringstyper = listOf(
                Næringstyper.JORDBRUK_SKOGBRUK,
                Næringstyper.FISKE,
                Næringstyper.DAGMAMMA,
                Næringstyper.ANNEN
            ),
            fraOgMed = fraOgMed,
            tilOgMed = LocalDate.parse("2021-01-10"),
            næringsinntekt = 123123,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = JaNei.Ja,
            organisasjonsnummer = "101010",
            erNyoppstartet = false
        ).validate(0)

        assertEquals(feil.size, 1)
        assertEquals(
            feil.first(),
            Violation(
                parameterName = "selvstendigVirksomheter[0].erNyoppstartet",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis erNyoppstartet er true så må fraOgMed være etter ${LocalDate.now().minusYears(4)}",
                invalidValue = fraOgMed
            )
        )
    }

    @Test
    internal fun `Virksomhet anses som nyoppstartet når fraOgMed er mindre enn 4 år siden`() {
        val fraOgMed = LocalDate.now().minusYears(4).minusDays(1)
        val feil: MutableSet<Violation> = Virksomhet(
            næringstyper = listOf(
                Næringstyper.JORDBRUK_SKOGBRUK,
                Næringstyper.FISKE,
                Næringstyper.DAGMAMMA,
                Næringstyper.ANNEN
            ),
            fraOgMed = fraOgMed,
            tilOgMed = LocalDate.parse("2021-01-10"),
            næringsinntekt = 123123,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = JaNei.Ja,
            organisasjonsnummer = "101010",
            erNyoppstartet = false
        ).validate(0)

        assertEquals(feil.size, 1)
        assertEquals(
            feil.first(),
            Violation(
                parameterName = "selvstendigVirksomheter[0].erNyoppstartet",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis erNyoppstartet er true så må fraOgMed være etter ${LocalDate.now().minusYears(4)}",
                invalidValue = fraOgMed
            )
        )
    }

}
