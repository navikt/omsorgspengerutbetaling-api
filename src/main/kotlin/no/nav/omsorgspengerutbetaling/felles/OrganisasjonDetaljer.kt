package no.nav.omsorgspengerutbetaling.felles

import no.nav.helse.arbeidsgiver.ArbeidsgiverDetaljer

data class OrganisasjonDetaljer(
    val navn: String? = null,
    val organisasjonsnummer: String,
    val harHattFraværHosArbeidsgiver: Boolean,
    val arbeidsgiverHarUtbetaltLønn: Boolean,
    val perioder: List<Utbetalingsperiode>
)

fun ArbeidsgiverDetaljer.valider() = organisasjoner.flatMap { it.perioder.validerUenVedlegg() }
