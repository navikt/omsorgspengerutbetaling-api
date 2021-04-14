package no.nav.omsorgspengerutbetaling.k9format

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.fravær.AktivitetFravær as K9AktivitetFravær
import no.nav.k9.søknad.felles.fravær.FraværPeriode
import no.nav.k9.søknad.felles.fravær.FraværÅrsak
import no.nav.k9.søknad.felles.opptjening.*
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Søker
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetaling
import no.nav.omsorgspengerutbetaling.soknad.*
import java.math.BigDecimal
import java.time.Duration
import java.time.ZonedDateTime
import no.nav.omsorgspengerutbetaling.soknad.Søknad as OmsorgspengerutbetalingSoknadSøknad

fun OmsorgspengerutbetalingSoknadSøknad.tilKOmsorgspengerUtbetalingSøknad(
    mottatt: ZonedDateTime,
    søker: no.nav.omsorgspengerutbetaling.soker.Søker
): Søknad {

    return Søknad(
        søknadId,
        Versjon.of("1.0.0"),
        mottatt,
        søker.tilK9Søker(),
        OmsorgspengerUtbetaling(
            fosterbarn?.tilK9Barn(),
            opptjeningAktivitet(),
            this.utbetalingsperioder.tilFraværsperiode(),
            this.bosteder.tilK9Bosteder(),
            this.opphold.tilK9Utenlandsopphold()
        )
    )
}

fun List<Opphold>.tilK9Utenlandsopphold(): Utenlandsopphold {
    val perioder = mutableMapOf<Periode, Utenlandsopphold.UtenlandsoppholdPeriodeInfo>()
    forEach {

        val periode = Periode(it.fraOgMed, it.tilOgMed)
        perioder[periode] = Utenlandsopphold.UtenlandsoppholdPeriodeInfo.builder()
            .land(Landkode.of(it.landkode))
            .build()
    }
    return Utenlandsopphold(perioder)
}

private fun List<Bosted>.tilK9Bosteder(): Bosteder {
    val perioder = mutableMapOf<Periode, Bosteder.BostedPeriodeInfo>()
    forEach {
        val periode = Periode(it.fraOgMed, it.tilOgMed)
        perioder[periode] = Bosteder.BostedPeriodeInfo(Landkode.of(it.landkode))
    }

    return Bosteder(perioder)
}

fun List<Utbetalingsperiode>.tilFraværsperiode(): List<FraværPeriode> = map { utbetalingsperiode ->
    FraværPeriode(
        Periode(utbetalingsperiode.fraOgMed, utbetalingsperiode.tilOgMed),
        utbetalingsperiode.antallTimerBorte,
        utbetalingsperiode.årsak?.let { FraværÅrsak.valueOf(it.name) } ?: FraværÅrsak.ORDINÆRT_FRAVÆR,
        utbetalingsperiode.aktivitetFravær.map {
            when(it) {
                AktivitetFravær.FRILANSER -> K9AktivitetFravær.FRILANSER
                AktivitetFravær.SELVSTENDIG_VIRKSOMHET -> K9AktivitetFravær.SELVSTENDIG_VIRKSOMHET
            }
        }
    )
}

fun OmsorgspengerutbetalingSoknadSøknad.opptjeningAktivitet() = OpptjeningAktivitet(
    null,
    selvstendigVirksomheter.tilK9SelvstendingNæringsdrivende(),
    frilans?.tilK9Frilanser()
)

private fun List<Virksomhet>.tilK9SelvstendingNæringsdrivende(): List<SelvstendigNæringsdrivende> = map { virksomhet ->
    SelvstendigNæringsdrivende(
        mapOf(Periode(virksomhet.fraOgMed, virksomhet.tilOgMed) to virksomhet.tilK9SelvstendingNæringsdrivendeInfo()),
        Organisasjonsnummer.of(virksomhet.organisasjonsnummer),
        virksomhet.navnPåVirksomheten
    )
}

fun Virksomhet.tilK9SelvstendingNæringsdrivendeInfo(): SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo {
    val infoBuilder = SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo.builder()
    infoBuilder
        .virksomhetstyper(næringstyper.tilK9Virksomhetstyper())
        .registrertIUtlandet(!registrertINorge.boolean)

    if (registrertINorge.boolean) infoBuilder.landkode(Landkode.NORGE)
    else infoBuilder.landkode(Landkode.of(registrertIUtlandet?.landkode))

    næringsinntekt?.let { infoBuilder.bruttoInntekt(BigDecimal.valueOf(it.toLong())) }

    infoBuilder.erNyoppstartet(erNyoppstartet)

    regnskapsfører?.let {
        infoBuilder
            .regnskapsførerNavn(it.navn)
            .regnskapsførerTelefon(it.telefon)
    }

    varigEndring?.let {
        infoBuilder
            .erVarigEndring(true)
            .endringDato(it.dato)
            .endringBegrunnelse(it.forklaring)
    } ?: infoBuilder.erVarigEndring(false)

    return infoBuilder.build()
}

private fun List<Næringstyper>.tilK9Virksomhetstyper(): List<VirksomhetType> = map {
    when (it) {
        Næringstyper.FISKE -> VirksomhetType.FISKE
        Næringstyper.JORDBRUK_SKOGBRUK -> VirksomhetType.JORDBRUK_SKOGBRUK
        Næringstyper.DAGMAMMA -> VirksomhetType.DAGMAMMA
        Næringstyper.ANNEN -> VirksomhetType.ANNEN
    }
}

private fun Frilans.tilK9Frilanser(): Frilanser = Frilanser()
    .medStartDato(startdato)
    .medSluttDato(sluttdato)
    .medJobberFortsattSomFrilans(jobberFortsattSomFrilans.boolean)

private fun List<FosterBarn>.tilK9Barn(): List<Barn> {
    return map {
        Barn(NorskIdentitetsnummer.of(it.fødselsnummer), null)
    }
}

private fun no.nav.omsorgspengerutbetaling.soker.Søker.tilK9Søker() = Søker(NorskIdentitetsnummer.of(fødselsnummer))
