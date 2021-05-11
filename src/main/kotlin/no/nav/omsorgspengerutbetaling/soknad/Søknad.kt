package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonValue
import no.nav.k9.søknad.felles.type.SøknadId
import java.net.URL
import java.util.*

data class Søknad(
    val søknadId: SøknadId = SøknadId(UUID.randomUUID().toString()),
    val språk: Språk,
    val harDekketTiFørsteDagerSelv: Boolean,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val spørsmål: List<SpørsmålOgSvar>,
    val bekreftelser: Bekreftelser,
    val utbetalingsperioder: List<Utbetalingsperiode>,
    val andreUtbetalinger: List<String>,
    val erArbeidstakerOgså: Boolean,
    val fosterbarn: List<FosterBarn>? = listOf(),
    val frilans: Frilans? = null,
    val selvstendigVirksomheter: List<Virksomhet> = listOf(),
    val vedlegg: List<URL>? = listOf()
)

enum class Språk(@JsonValue val språk: String) {
    BOKMÅL("nb"),
    NYNORSK("nn");
}
