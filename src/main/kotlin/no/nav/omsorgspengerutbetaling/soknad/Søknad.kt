package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonValue
import java.net.URL
import no.nav.omsorgspengerutbetaling.felles.*

data class Søknad(
    val språk: Språk,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val spørsmål: List<SpørsmålOgSvar>,
    val bekreftelser: Bekreftelser,
    val utbetalingsperioder: List<UtbetalingsperiodeMedVedlegg>,
    val andreUtbetalinger: List<String>?, //TODO: Fjern ? når dette er prodsatt.
    val erArbeidstakerOgså: Boolean,
    val fosterbarn: List<FosterBarn>? = listOf(),
    val frilans: Frilans? = null,
    val selvstendigVirksomheter: List<Virksomhet> = listOf(),
    val vedlegg: List<URL>? = listOf(),
    val hjemmePgaSmittevernhensyn: Boolean
)

enum class Språk(@JsonValue val språk: String) {
    BOKMÅL("nb"),
    NYNORSK("nn");
}

