package no.nav.omsorgspengerutbetaling.soknad

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.omsorgspengerutbetaling.soker.Søker
import java.net.URL
import java.time.ZonedDateTime

data class KomplettSoknad(
    val søknadId: SøknadId,
    val språk: Språk,
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val harDekketTiFørsteDagerSelv: Boolean,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val spørsmål: List<SpørsmålOgSvar>,
    val bekreftelser: Bekreftelser,
    val utbetalingsperioder: List<Utbetalingsperiode>,
    val andreUtbetalinger: List<String>,
    val erArbeidstakerOgså: Boolean,
    val vedlegg: List<URL> = listOf(),
    val fosterbarn: List<FosterBarn>? = listOf(),
    val frilans: Frilans? = null,
    val selvstendigVirksomheter: List<Virksomhet> = listOf(),
    val k9FormatSøknad: Søknad
)