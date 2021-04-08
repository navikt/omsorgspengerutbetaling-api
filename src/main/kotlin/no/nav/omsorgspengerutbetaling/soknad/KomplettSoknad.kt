package no.nav.omsorgspengerutbetaling.soknad

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.vedlegg.Vedlegg
import java.time.ZonedDateTime

data class KomplettSoknad(
    val søknadId: SøknadId,
    val språk: Språk,
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val harDekketTiFørsteDagerSelv: Boolean? = null, // TODO: 08/04/2021 Fjern nullable etter prodsetting.
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val spørsmål: List<SpørsmålOgSvar>,
    val bekreftelser: Bekreftelser,
    val utbetalingsperioder: List<Utbetalingsperiode>,
    val andreUtbetalinger: List<String>?, //TODO: Fjern ? når dette er prodsatt.
    val erArbeidstakerOgså: Boolean,
    val vedlegg: List<Vedlegg> = listOf(),
    val fosterbarn: List<FosterBarn>? = listOf(),
    val frilans: Frilans? = null,
    val selvstendigVirksomheter: List<Virksomhet> = listOf(),
    val hjemmePgaSmittevernhensyn: Boolean? = null, // TODO: 15/03/2021 utgår
    val hjemmePgaStengtBhgSkole: Boolean? = null, // TODO: 15/03/2021 utgår
    val k9FormatSøknad: Søknad
)
