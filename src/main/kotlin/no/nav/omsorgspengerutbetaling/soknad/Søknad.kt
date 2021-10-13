package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonValue
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.omsorgspengerutbetaling.soker.Søker
import java.net.URL
import java.util.*

data class Søknad(
    val søknadId: SøknadId = SøknadId(UUID.randomUUID().toString()),
    val språk: Språk,
    val harDekketTiFørsteDagerSelv: Boolean? = null,
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
    val vedlegg: List<URL> = listOf()
) {
    fun tilKomplettSøknad(k9Format: Søknad, søker: Søker) = KomplettSoknad(
        søknadId = søknadId,
        språk = språk,
        mottatt = k9Format.mottattDato,
        søker = søker,
        harDekketTiFørsteDagerSelv = harDekketTiFørsteDagerSelv!!,
        bosteder = bosteder,
        opphold = opphold,
        spørsmål = spørsmål,
        utbetalingsperioder = utbetalingsperioder,
        andreUtbetalinger = andreUtbetalinger,
        vedlegg = vedlegg,
        frilans = frilans,
        fosterbarn = fosterbarn,
        selvstendigVirksomheter = selvstendigVirksomheter,
        erArbeidstakerOgså = erArbeidstakerOgså,
        bekreftelser = bekreftelser,
        k9FormatSøknad = k9Format
    )
}

enum class Språk(@JsonValue val språk: String) {
    BOKMÅL("nb"),
    NYNORSK("nn");
}