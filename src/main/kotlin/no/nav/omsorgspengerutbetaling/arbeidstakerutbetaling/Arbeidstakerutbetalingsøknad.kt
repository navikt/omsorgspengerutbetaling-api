package no.nav.omsorgspengerutbetaling.arbeidstakerutbetaling

import no.nav.helse.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.felles.*
import java.net.URL

data class Arbeidstakerutbetalingsøknad(
    val språk: Språk,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val spørsmål: List<SpørsmålOgSvar>,
    val jobbHosNåværendeArbeidsgiver: JobbHosNåværendeArbeidsgiver,
    val arbeidsgivere: ArbeidsgiverDetaljer,
    val bekreftelser: Bekreftelser,
    val andreUtbetalinger: List<String>,
    val fosterbarn: List<FosterBarn>? = listOf(),
    val vedlegg: List<URL>
)
