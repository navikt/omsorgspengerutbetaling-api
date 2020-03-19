package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonValue

data class Søknad(
    val språk: Språk,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val spørsmål: List<SpørsmålOgSvar>,
    val utbetalingsperioder: List<UtbetalingsperiodeMedVedlegg>,
    val harHattInntektSomFrilanser: Boolean = false,
    val frilans: Frilans? = null,
    val harHattInntektSomSelvstendigNaringsdrivende: Boolean = false,
    val selvstendigVirksomheter: List<Virksomhet>? = null
)

enum class Språk(@JsonValue val språk: String) {
    BOKMÅL("nb"),
    NYNORSK("nn");
}
