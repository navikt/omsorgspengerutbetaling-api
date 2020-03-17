package no.nav.omsorgspengerutbetaling.soknad

import no.nav.omsorgspengerutbetaling.soker.Søker
import java.time.ZonedDateTime

data class KomplettSoknad(
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val språk: Språk,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val jaNei: List<JaNei>,
    val utbetalingsperioder: List<UtbetalingsperiodeVedlegg>
)