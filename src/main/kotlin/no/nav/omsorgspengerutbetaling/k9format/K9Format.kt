package no.nav.omsorgspengerutbetaling.k9format

import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.fravær.FraværPeriode
import no.nav.k9.søknad.felles.fravær.FraværÅrsak
import no.nav.k9.søknad.felles.opptjening.Frilanser
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Søker
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.type.*
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetaling
import no.nav.omsorgspengerutbetaling.soknad.*
import java.math.BigDecimal
import java.time.ZonedDateTime
import no.nav.k9.søknad.Søknad as K9Søknad
import no.nav.k9.søknad.felles.fravær.AktivitetFravær as K9AktivitetFravær
import no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende as K9SelvstendigNæringsdrivende
import no.nav.omsorgspengerutbetaling.soknad.Søknad as OmsorgspengerutbetalingSoknadSøknad

fun OmsorgspengerutbetalingSoknadSøknad.tilKOmsorgspengerUtbetalingSøknad(
    mottatt: ZonedDateTime,
    søker: no.nav.omsorgspengerutbetaling.soker.Søker
): K9Søknad {

    return K9Søknad(
        søknadId,
        Versjon.of("1.0.0"),
        mottatt,
        søker.tilK9Søker(),
        OmsorgspengerUtbetaling(
            fosterbarn?.tilK9Barn(),
            opptjeningAktivitet(),
            this.utbetalingsperioder.tilFraværsperiode(),
            null,
            this.bosteder.tilK9Bosteder(),
            this.opphold.tilK9Utenlandsopphold()
        )
    )
}

fun List<Opphold>.tilK9Utenlandsopphold(): Utenlandsopphold {
    val perioder = mutableMapOf<Periode, Utenlandsopphold.UtenlandsoppholdPeriodeInfo>()
    forEach {
        val periode = Periode(it.fraOgMed, it.tilOgMed)
        perioder[periode] = Utenlandsopphold.UtenlandsoppholdPeriodeInfo()
            .medLand(Landkode.of(it.landkode))
    }
    return Utenlandsopphold().medPerioder(perioder)
}

private fun List<Bosted>.tilK9Bosteder(): Bosteder {
    val perioder = mutableMapOf<Periode, Bosteder.BostedPeriodeInfo>()
    forEach {
        val periode = Periode(it.fraOgMed, it.tilOgMed)
        perioder[periode] = Bosteder.BostedPeriodeInfo().medLand(Landkode.of(it.landkode))
    }

    return Bosteder().medPerioder(perioder)
}

fun List<Utbetalingsperiode>.tilFraværsperiode(): List<FraværPeriode> = map { utbetalingsperiode ->
    FraværPeriode(
        Periode(utbetalingsperiode.fraOgMed, utbetalingsperiode.tilOgMed),
        utbetalingsperiode.antallTimerBorte,
        FraværÅrsak.valueOf(utbetalingsperiode.årsak.name),
        null,
        utbetalingsperiode.aktivitetFravær.map {
            when (it) {
                AktivitetFravær.FRILANSER -> K9AktivitetFravær.FRILANSER
                AktivitetFravær.SELVSTENDIG_VIRKSOMHET -> K9AktivitetFravær.SELVSTENDIG_VIRKSOMHET
            }
        },
        null,
        null
    )
}

fun OmsorgspengerutbetalingSoknadSøknad.opptjeningAktivitet(): OpptjeningAktivitet {
    val opptjeningAktivitet = OpptjeningAktivitet()

    selvstendigNæringsdrivende?.let {
        opptjeningAktivitet.medSelvstendigNæringsdrivende(
            K9SelvstendigNæringsdrivende()
                .medVirksomhetNavn(it.navnPåVirksomheten)
                .medPerioder(mapOf(Periode(it.fraOgMed, it.tilOgMed) to it.tilK9SelvstendingNæringsdrivendeInfo()))
                .apply {
                    it.organisasjonsnummer?.let {
                        medOrganisasjonsnummer(Organisasjonsnummer.of(it))
                    }
                }
        )
    }

    frilans?.let {
        opptjeningAktivitet.medFrilanser(
            it.tilK9Frilanser()
        )
    }

    return opptjeningAktivitet
}

fun SelvstendigNæringsdrivende.tilK9SelvstendingNæringsdrivendeInfo(): K9SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo {
    val infoBuilder = K9SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo.builder()
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
            .bruttoInntekt(BigDecimal.valueOf(it.inntektEtterEndring.toLong()))
            .endringDato(it.dato)
            .endringBegrunnelse(it.forklaring)
    } ?: infoBuilder.erVarigEndring(false)

    yrkesaktivSisteTreFerdigliknedeÅrene?.let { infoBuilder.erNyIArbeidslivet(true) }
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

private fun Frilans.tilK9Frilanser(): Frilanser = Frilanser().apply {
    medStartDato(this@tilK9Frilanser.startdato)
    this@tilK9Frilanser.sluttdato?.let { medStartDato(it) }
}

private fun List<FosterBarn>.tilK9Barn(): List<Barn> {
    return map {
        Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(it.fødselsnummer))
    }
}

private fun no.nav.omsorgspengerutbetaling.soker.Søker.tilK9Søker() = Søker(NorskIdentitetsnummer.of(fødselsnummer))