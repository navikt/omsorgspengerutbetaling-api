package no.nav.omsorgspengerutbetaling.validering

import no.nav.omsorgspengerutbetaling.SøknadUtils
import no.nav.omsorgspengerutbetaling.TestUtils.Companion.validerOgAssertMangler
import no.nav.omsorgspengerutbetaling.soknad.JaNei
import no.nav.omsorgspengerutbetaling.soknad.Land
import no.nav.omsorgspengerutbetaling.soknad.Næringstyper
import no.nav.omsorgspengerutbetaling.soknad.SelvstendigNæringsdrivende
import java.time.LocalDate
import kotlin.test.Test

internal class SelvstendigNæringsdrivendeValideringTest {

    val gyldigSelvstendigNæringsdrivende = SelvstendigNæringsdrivende(
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

    val gyldigSøknad = SøknadUtils.hentGyldigSøknad().copy(
        selvstendigNæringsdrivende = gyldigSelvstendigNæringsdrivende
    )

    @Test
    fun `Gyldig søknad gir ingen feil`(){
        validerOgAssertMangler(gyldigSøknad, false)
    }

    @Test
    internal fun `Dersom organisasjonsnummer er ugydig skal det gi feil`() {
        val søknad = gyldigSøknad.copy(
            selvstendigNæringsdrivende = gyldigSelvstendigNæringsdrivende.copy(
                organisasjonsnummer = "123ABC"
            )
        )

        val forventetMangler = """
            [{
                "reason": "Ugyldig organisasjonsnummer, inneholder noe annet enn tall.",
                "name": "selvstendigNæringsdrivende.organisasjonsnummer",
                "invalid_value": null,
                "type": "entity"
             }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    internal fun `Dersom registrertIUtlandet kun inneholder blanke verdier skal det gi feil`() {
        val søknad = gyldigSøknad.copy(
            selvstendigNæringsdrivende = gyldigSelvstendigNæringsdrivende.copy(
                registrertIUtlandet = Land(
                    landkode = " ",
                    landnavn = " "
                )
            )
        )

        val forventetMangler = """
            [
              {
                "reason": "Landkode er ikke en gyldig ISO 3166-1 alpha-3 kode.",
                "name": "selvstendigNæringsdrivende.landkode",
                "invalid_value": " ",
                "type": "entity"
              },
              {
                "reason": "Landkode kan ikke være blank.",
                "name": "selvstendigNæringsdrivende.landkode",
                "invalid_value": " ",
                "type": "entity"
              },
              {
                "reason": "Landnavn kan ikke være blank.",
                "name": "selvstendigNæringsdrivende.landnavn",
                "invalid_value": " ",
                "type": "entity"
              }
            ]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    internal fun `Gitt at nyoppstartet er true og fraOgMed er etter 4 år siden, forvent feil`() {
        val fraOgMed = LocalDate.now().minusYears(4).minusDays(1)
        val søknad = gyldigSøknad.copy(
            selvstendigNæringsdrivende = gyldigSelvstendigNæringsdrivende.copy(
                fraOgMed = fraOgMed,
                erNyoppstartet = true
            )
        )

        val forventetMangler = """
            [{
                "reason": "Hvis erNyoppstartet er true så må fraOgMed være etter ${fraOgMed.plusDays(1)}",
                "name": "selvstendigNæringsdrivende.erNyoppstartet",
                "invalid_value": "$fraOgMed",
                "type": "entity"
             }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    internal fun `Gitt at nyoppstartet er true og fraOgMed er akkurat 4 år siden, forvent feil`() {
        val fraOgMed = LocalDate.now().minusYears(4)
        val søknad = gyldigSøknad.copy(
            selvstendigNæringsdrivende = gyldigSelvstendigNæringsdrivende.copy(
                fraOgMed = fraOgMed,
                erNyoppstartet = true
            )
        )

        val forventetMangler = """
            [{
                "reason": "Hvis erNyoppstartet er true så må fraOgMed være etter $fraOgMed",
                "name": "selvstendigNæringsdrivende.erNyoppstartet",
                "invalid_value": "$fraOgMed",
                "type": "entity"
             }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    internal fun `Gitt at nyoppstartet er true og fraOgMed er før 4 år siden, forvent ingen feil`() {
        val fraOgMed = LocalDate.now().minusYears(4).plusDays(1)
        val søknad = gyldigSøknad.copy(
            selvstendigNæringsdrivende = gyldigSelvstendigNæringsdrivende.copy(
                fraOgMed = fraOgMed,
                erNyoppstartet = true
            )
        )
        validerOgAssertMangler(søknad, false)
    }

    @Test
    internal fun `Gitt at nyoppstartet er false og fraOgMed er før 4 år siden, forvent feil`() {
        val fraOgMed = LocalDate.now().minusYears(4).plusDays(1)
        val søknad = gyldigSøknad.copy(
            selvstendigNæringsdrivende = gyldigSelvstendigNæringsdrivende.copy(
                fraOgMed = fraOgMed,
                erNyoppstartet = false
            )
        )

        val forventetMangler = """
            [{
                "reason": "Hvis erNyoppstartet er false så må fraOgMed være før ${LocalDate.now().minusYears(4)}",
                "name": "selvstendigNæringsdrivende.erNyoppstartet",
                "invalid_value": "$fraOgMed",
                "type": "entity"
             }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    internal fun `Gitt at nyoppstartet er false og fraOgMed er akkurat 4 år siden, forvent ingen feil`() {
        val fraOgMed = LocalDate.now().minusYears(4)
        val søknad = gyldigSøknad.copy(
            selvstendigNæringsdrivende = gyldigSelvstendigNæringsdrivende.copy(
                fraOgMed = fraOgMed,
                erNyoppstartet = false
            )
        )

        validerOgAssertMangler(søknad, false)
    }

    @Test
    internal fun `Gitt at nyoppstartet er false og fraOgMed er etter 4 år siden, forvent ingen feil`() {
        val fraOgMed = LocalDate.now().minusYears(4).minusDays(1)
        val søknad = gyldigSøknad.copy(
            selvstendigNæringsdrivende = gyldigSelvstendigNæringsdrivende.copy(
                fraOgMed = fraOgMed,
                erNyoppstartet = false
            )
        )

        validerOgAssertMangler(søknad, false)
    }

    @Test
    internal fun `harFlereAktiveVirksomheter må settes, ved null skal det gis feil`() {
        val søknad = gyldigSøknad.copy(
            selvstendigNæringsdrivende = gyldigSelvstendigNæringsdrivende.copy(
                harFlereAktiveVirksomheter = null
            )
        )
        val forventetMangler = """
            [{
                "reason": "harFlereAktiveVirksomheter må være satt til true eller false, ikke null",
                "name": "selvstendigNæringsdrivende.harFlereAktiveVirksomheter",
                "invalid_value": null,
                "type": "entity"
             }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }
}