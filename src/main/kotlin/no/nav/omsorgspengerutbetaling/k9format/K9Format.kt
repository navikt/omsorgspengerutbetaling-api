package no.nav.omsorgspengerutbetaling.k9format

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.aktivitet.*
import no.nav.k9.søknad.felles.fravær.FraværPeriode
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
import java.time.LocalDate
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
            arbeidAktivitet(),
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

fun List<UtbetalingsperiodeMedVedlegg>.tilFraværsperiode(): List<FraværPeriode> = map {
    FraværPeriode(Periode(it.fraOgMed, it.tilOgMed), it.lengde)
}

fun OmsorgspengerutbetalingSoknadSøknad.arbeidAktivitet() = ArbeidAktivitet.builder()
    .frilanser(frilans?.tilK9Frilanser())
    .selvstendigNæringsdrivende(selvstendigVirksomheter.tilK9SelvstendingNæringsdrivende())
    .build()

private fun List<Virksomhet>.tilK9SelvstendingNæringsdrivende(): List<SelvstendigNæringsdrivende> = map { virksomhet ->
    val builder = SelvstendigNæringsdrivende.builder()
        .virksomhetNavn(virksomhet.navnPåVirksomheten)
        .periode(
            Periode(virksomhet.fraOgMed, virksomhet.tilOgMed),
            virksomhet.tilK9SelvstendingNæringsdrivendeInfo()
        )

    virksomhet.organisasjonsnummer?.let { builder.organisasjonsnummer(Organisasjonsnummer.of(it)) }

    builder.build()
}

fun Virksomhet.tilK9SelvstendingNæringsdrivendeInfo(): SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo {
    val infoBuilder = SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo.builder()
    infoBuilder
        .virksomhetstyper(næringstyper.tilK9Virksomhetstyper())
        .registrertIUtlandet(!registrertINorge.boolean)

    if (registrertINorge.boolean) infoBuilder.landkode(Landkode.NORGE)
    else infoBuilder.landkode(Landkode.of(registrertIUtlandet?.landkode))

    næringsinntekt?.let { infoBuilder.bruttoInntekt(BigDecimal.valueOf(it.toLong())) }

    when (erEldreEnn3År()) {
        true -> infoBuilder.erNyoppstartet(false)
        false -> infoBuilder.erNyoppstartet(true)
    }

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

private fun Virksomhet.erEldreEnn3År() = fraOgMed.isBefore(LocalDate.now().minusYears(3)) || fraOgMed.isEqual(LocalDate.now().minusYears(3))

private fun List<Næringstyper>.tilK9Virksomhetstyper(): List<VirksomhetType> = map {
    when (it) {
        Næringstyper.FISKE -> VirksomhetType.FISKE
        Næringstyper.JORDBRUK_SKOGBRUK -> VirksomhetType.JORDBRUK_SKOGBRUK
        Næringstyper.DAGMAMMA -> VirksomhetType.DAGMAMMA
        Næringstyper.ANNEN -> VirksomhetType.ANNEN
    }
}

private fun Frilans.tilK9Frilanser(): Frilanser = Frilanser.builder()
    .startdato(startdato)
    .jobberFortsattSomFrilans(jobberFortsattSomFrilans.boolean)
    .build()


private fun List<FosterBarn>.tilK9Barn(): List<Barn> {
    return map {
        Barn(NorskIdentitetsnummer.of(it.fødselsnummer), null)
    }
}

private fun no.nav.omsorgspengerutbetaling.soker.Søker.tilK9Søker() = Søker(NorskIdentitetsnummer.of(fødselsnummer))
