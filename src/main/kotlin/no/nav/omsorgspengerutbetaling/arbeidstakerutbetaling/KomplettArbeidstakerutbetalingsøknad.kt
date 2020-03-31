package no.nav.omsorgspengerutbetaling.arbeidstakerutbetaling

import no.nav.helse.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.felles.*
import no.nav.omsorgspengerutbetaling.soker.Søker
import java.time.ZonedDateTime

data class KomplettArbeidstakerutbetalingsøknad(
    val språk: Språk,
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val arbeidsgivere: ArbeidsgiverDetaljer,
    val spørsmål: List<SpørsmålOgSvar>,
    val bekreftelser: Bekreftelser,
    val utbetalingsperioder: List<Utbetalingsperiode>,
    val fosterbarn: List<FosterBarn>? = listOf()
)
