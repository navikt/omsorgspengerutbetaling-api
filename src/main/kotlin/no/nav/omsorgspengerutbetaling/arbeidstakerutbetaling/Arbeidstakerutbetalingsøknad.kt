package no.nav.omsorgspengerutbetaling.arbeidstakerutbetaling

import no.nav.helse.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.felles.*

data class Arbeidstakerutbetalingsøknad(
    val språk: Språk,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val spørsmål: List<SpørsmålOgSvar>,
    val arbeidsgivere: ArbeidsgiverDetaljer,
    val bekreftelser: Bekreftelser,
    val utbetalingsperioder: List<Utbetalingsperiode>,
    val andreUtbetalinger: List<String>,
    val fosterbarn: List<FosterBarn>? = listOf()
)
