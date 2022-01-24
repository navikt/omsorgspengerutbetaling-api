package no.nav.omsorgspengerutbetaling.validering

import no.nav.omsorgspengerutbetaling.SøknadUtils
import no.nav.omsorgspengerutbetaling.TestUtils.Companion.validerOgAssertMangler
import no.nav.omsorgspengerutbetaling.soknad.AktivitetFravær
import no.nav.omsorgspengerutbetaling.soknad.FraværÅrsak
import no.nav.omsorgspengerutbetaling.soknad.Utbetalingsperiode
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test

class UtbetalingsperiodeValideringTest {
    val gyldigUtbetalingsperiode = Utbetalingsperiode(
        fraOgMed = LocalDate.parse("2015-01-01"),
        tilOgMed = LocalDate.parse("2015-01-10"),
        antallTimerPlanlagt = Duration.ofHours(5),
        antallTimerBorte = Duration.ofHours(3),
        årsak = FraværÅrsak.STENGT_SKOLE_ELLER_BARNEHAGE,
        aktivitetFravær = listOf(AktivitetFravær.FRILANSER)
    )

    val gyldigSøknad = SøknadUtils.hentGyldigSøknad().copy(
        utbetalingsperioder = listOf(gyldigUtbetalingsperiode)
    )

    @Test
    fun `Gyldig søknad gir ingen feil`(){
        validerOgAssertMangler(gyldigSøknad, false)
    }

    @Test
    fun `Skal gi feil dersom listen er tom`(){
        val søknad = gyldigSøknad.copy(
            utbetalingsperioder = listOf()
        )
        val forventetMangler = """
              [{
                "reason": "Må settes minst en utbetalingsperiode.",
                "name": "utbetalingsperioder",
                "invalid_value": [],
                "type": "entity"
              }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    fun `Skal gi feil dersom fraOgMed er etter tilOgMed`(){
        val søknad = gyldigSøknad.copy(
            utbetalingsperioder = listOf(
                gyldigUtbetalingsperiode.copy(
                    fraOgMed = LocalDate.parse("2015-01-02"),
                    tilOgMed = LocalDate.parse("2015-01-01"),
                )
            )
        )
        val forventetMangler = """
              [{
                "reason": "Til og med må være etter eller lik fra og med",
                "name": "utbetalingsperioder[0]",
                "invalid_value": "2015-01-01",
                "type": "entity"
              }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    fun `Skal gi feil dersom antallTimerPlanlagt er satt og antallTimerBorte er null`(){
        val søknad = gyldigSøknad.copy(
            utbetalingsperioder = listOf(
                gyldigUtbetalingsperiode.copy(
                    antallTimerPlanlagt = Duration.ofHours(5),
                    antallTimerBorte = null
                )
            )
        )
        val forventetMangler = """
              [ {
                "reason": "Dersom antallTimerPlanlagt er satt så kan ikke antallTimerBorte være tom",
                "name": "utbetalingsperioder[0]",
                "invalid_value": "antallTimerBorte = null, antallTimerPlanlagt=PT5H",
                "type": "entity"
              }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    fun `Skal gi feil dersom antallTimerBorte er satt og antallTimerPlanlagt er null`(){
        val søknad = gyldigSøknad.copy(
            utbetalingsperioder = listOf(
                gyldigUtbetalingsperiode.copy(
                    antallTimerBorte = Duration.ofHours(5),
                    antallTimerPlanlagt = null
                )
            )
        )
        val forventetMangler = """
              [ {
                "reason": "Dersom antallTimerBorte er satt så kan ikke antallTimerPlanlagt være tom",
                "name": "utbetalingsperioder[0]",
                "invalid_value": "antallTimerBorte = PT5H, antallTimerPlanlagt=null",
                "type": "entity"
              }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }

    @Test
    fun `Skal gi feil dersom antallTimerBorte er større enn antallTimerPlanlagt`(){
        val søknad = gyldigSøknad.copy(
            utbetalingsperioder = listOf(
                gyldigUtbetalingsperiode.copy(
                    antallTimerBorte = Duration.ofHours(5),
                    antallTimerPlanlagt = Duration.ofHours(4)
                )
            )
        )
        val forventetMangler = """
              [ {
                "reason": "Antall timer borte kan ikke være større enn antall timer planlagt jobbe",
                "name": "utbetalingsperioder[0]",
                "invalid_value": "antallTimerBorte = PT5H, antallTimerPlanlagt=PT4H",
                "type": "entity"
              }]
        """.trimIndent()
        validerOgAssertMangler(søknad, true, forventetMangler)
    }
}