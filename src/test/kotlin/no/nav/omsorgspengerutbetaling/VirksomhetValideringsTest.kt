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

internal class VirksomhetValideringsTest {

    @Test
    internal fun `Gitt at nyoppstartet er true og fraOgMed er etter 4 år siden, forvent feil`() {
        val fraOgMed = LocalDate.now().minusYears(4).minusDays(1)
        val feil: MutableSet<Violation> = Virksomhet(
            næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
            fraOgMed = fraOgMed,
            tilOgMed = LocalDate.parse("2021-01-10"),
            næringsinntekt = 123123,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = JaNei.Ja,
            organisasjonsnummer = "101010",
            erNyoppstartet = true
        ).validate(0)

        assertEquals(1, feil.size)
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
    internal fun `Gitt at nyoppstartet er true og fraOgMed er akkurat 4 år siden, forvent feil`() {
        val fraOgMed = LocalDate.now().minusYears(4)
        val feil: MutableSet<Violation> = Virksomhet(
            næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
            fraOgMed = fraOgMed,
            tilOgMed = LocalDate.parse("2021-01-10"),
            næringsinntekt = 123123,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = JaNei.Ja,
            organisasjonsnummer = "101010",
            erNyoppstartet = true
        ).validate(0)

        assertEquals(1, feil.size)
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
    internal fun `Gitt at nyoppstartet er true og fraOgMed er før 4 år siden, forvent ingen feil`() {
        val fraOgMed = LocalDate.now().minusYears(4).plusDays(1)
        val feil: MutableSet<Violation> = Virksomhet(
            næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
            fraOgMed = fraOgMed,
            tilOgMed = LocalDate.parse("2021-01-10"),
            næringsinntekt = 123123,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = JaNei.Ja,
            organisasjonsnummer = "101010",
            erNyoppstartet = true
        ).validate(0)

        assertEquals(0, feil.size)
    }

    @Test
    internal fun `Gitt at nyoppstartet er false og fraOgMed er før 4 år siden, forvent feil`() {
        val fraOgMed = LocalDate.now().minusYears(4).plusDays(1)
        val feil: MutableSet<Violation> = Virksomhet(
            næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
            fraOgMed = fraOgMed,
            tilOgMed = LocalDate.parse("2021-01-10"),
            næringsinntekt = 123123,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = JaNei.Ja,
            organisasjonsnummer = "101010",
            erNyoppstartet = false
        ).validate(0)

        assertEquals(1, feil.size)
        assertEquals(
            feil.first(),
            Violation(
                parameterName = "selvstendigVirksomheter[0].erNyoppstartet",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis erNyoppstartet er false så må fraOgMed være før ${LocalDate.now().minusYears(4)}",
                invalidValue = fraOgMed
            )
        )
    }

    @Test
    internal fun `Gitt at nyoppstartet er false og fraOgMed er akkurat 4 år siden, forvent ingen feil`() {
        val fraOgMed = LocalDate.now().minusYears(4)
        val feil: MutableSet<Violation> = Virksomhet(
            næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
            fraOgMed = fraOgMed,
            tilOgMed = LocalDate.parse("2021-01-10"),
            næringsinntekt = 123123,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = JaNei.Ja,
            organisasjonsnummer = "101010",
            erNyoppstartet = false
        ).validate(0)

        assertEquals(0, feil.size)
    }

    @Test
    internal fun `Gitt at nyoppstartet er false og fraOgMed er etter 4 år siden, forvent ingen feil`() {
        val fraOgMed = LocalDate.now().minusYears(4).minusDays(1)
        val feil: MutableSet<Violation> = Virksomhet(
            næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
            fraOgMed = fraOgMed,
            tilOgMed = LocalDate.parse("2021-01-10"),
            næringsinntekt = 123123,
            navnPåVirksomheten = "TullOgTøys",
            registrertINorge = JaNei.Ja,
            organisasjonsnummer = "101010",
            erNyoppstartet = false
        ).validate(0)

        assertEquals(0, feil.size)
    }
}
