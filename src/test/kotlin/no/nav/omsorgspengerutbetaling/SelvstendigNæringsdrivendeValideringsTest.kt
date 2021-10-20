package no.nav.omsorgspengerutbetaling

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgspengerutbetaling.soknad.JaNei
import no.nav.omsorgspengerutbetaling.soknad.Næringstyper
import no.nav.omsorgspengerutbetaling.soknad.SelvstendigNæringsdrivende
import no.nav.omsorgspengerutbetaling.soknad.validate
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SelvstendigNæringsdrivendeValideringsTest {

    val defaultSelvstendigNæringsdrivende = SelvstendigNæringsdrivende(
        næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
        fraOgMed = LocalDate.parse("2020-01-10"),
        tilOgMed = LocalDate.parse("2021-01-10"),
        næringsinntekt = 123123,
        navnPåVirksomheten = "TullOgTøys",
        registrertINorge = JaNei.Ja,
        organisasjonsnummer = "101010",
        erNyoppstartet = true,
        harFlereAktiveVirksomheter = false
    )

    @Test
    internal fun `Gitt at nyoppstartet er true og fraOgMed er etter 4 år siden, forvent feil`() {
        val fraOgMed = LocalDate.now().minusYears(4).minusDays(1)
        val feil = defaultSelvstendigNæringsdrivende.copy(
            fraOgMed = fraOgMed,
            erNyoppstartet = true
        ).validate()

        assertEquals(1, feil.size)
        assertEquals(
            feil.first(),
            Violation(
                parameterName = "selvstendigNæringsdrivende.erNyoppstartet",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis erNyoppstartet er true så må fraOgMed være etter ${LocalDate.now().minusYears(4)}",
                invalidValue = fraOgMed
            )
        )
    }

    @Test
    internal fun `Gitt at nyoppstartet er true og fraOgMed er akkurat 4 år siden, forvent feil`() {
        val fraOgMed = LocalDate.now().minusYears(4)
        val feil = defaultSelvstendigNæringsdrivende.copy(
            fraOgMed = fraOgMed,
            erNyoppstartet = true
        ).validate()

        assertEquals(1, feil.size)
        assertEquals(
            feil.first(),
            Violation(
                parameterName = "selvstendigNæringsdrivende.erNyoppstartet",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis erNyoppstartet er true så må fraOgMed være etter ${LocalDate.now().minusYears(4)}",
                invalidValue = fraOgMed
            )
        )
    }

    @Test
    internal fun `Gitt at nyoppstartet er true og fraOgMed er før 4 år siden, forvent ingen feil`() {
        val fraOgMed = LocalDate.now().minusYears(4).plusDays(1)
        val feil = defaultSelvstendigNæringsdrivende.copy(
            fraOgMed = fraOgMed,
            erNyoppstartet = true
        ).validate()

        assertEquals(0, feil.size)
    }

    @Test
    internal fun `Gitt at nyoppstartet er false og fraOgMed er før 4 år siden, forvent feil`() {
        val fraOgMed = LocalDate.now().minusYears(4).plusDays(1)
        val feil = defaultSelvstendigNæringsdrivende.copy(
            fraOgMed = fraOgMed,
            erNyoppstartet = false
        ).validate()

        assertEquals(1, feil.size)
        assertEquals(
            feil.first(),
            Violation(
                parameterName = "selvstendigNæringsdrivende.erNyoppstartet",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis erNyoppstartet er false så må fraOgMed være før ${LocalDate.now().minusYears(4)}",
                invalidValue = fraOgMed
            )
        )
    }

    @Test
    internal fun `Gitt at nyoppstartet er false og fraOgMed er akkurat 4 år siden, forvent ingen feil`() {
        val fraOgMed = LocalDate.now().minusYears(4)
        val feil = defaultSelvstendigNæringsdrivende.copy(
            fraOgMed = fraOgMed,
            erNyoppstartet = false
        ).validate()

        assertEquals(0, feil.size)
    }

    @Test
    internal fun `Gitt at nyoppstartet er false og fraOgMed er etter 4 år siden, forvent ingen feil`() {
        val fraOgMed = LocalDate.now().minusYears(4).minusDays(1)
        val feil = defaultSelvstendigNæringsdrivende.copy(
            fraOgMed = fraOgMed,
            erNyoppstartet = false
        ).validate()

        assertEquals(0, feil.size)
    }

    @Test
    internal fun `harFlereAktiveVirksomheter må settes, ved null skal det gis feil`() {
        val feil = defaultSelvstendigNæringsdrivende.copy(
            harFlereAktiveVirksomheter = null
        ).validate()

        assertEquals(1, feil.size)
        assertEquals(
            feil.first(),
            Violation(
                parameterName = "selvstendigNæringsdrivende.harFlereAktiveVirksomheter",
                parameterType = ParameterType.ENTITY,
                reason = "harFlereAktiveVirksomheter må være satt til true eller false, ikke null",
                invalidValue = null
            )
        )
    }
}