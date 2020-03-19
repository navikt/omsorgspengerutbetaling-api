package no.nav.omsorgspengerutbetaling.soknad

import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.vedlegg.Vedlegg
import java.time.ZonedDateTime

data class KomplettSoknad(
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val språk: Språk,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val spørsmål: List<SpørsmålOgSvar>,
    val utbetalingsperioder: List<UtbetalingsperiodeUtenVedlegg>,
    val vedlegg: List<Vedlegg>,
    val frilans: Frilans? = null,
    val selvstendigVirksomheter: List<Virksomhet>? = null
)
