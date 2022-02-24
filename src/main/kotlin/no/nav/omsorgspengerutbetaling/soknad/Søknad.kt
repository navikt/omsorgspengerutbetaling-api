package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonValue
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.omsorgspengerutbetaling.soker.Søker
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

data class Søknad(
    val søknadId: SøknadId = SøknadId(UUID.randomUUID().toString()),
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val språk: Språk,
    val harDekketTiFørsteDagerSelv: Boolean? = null,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val spørsmål: List<SpørsmålOgSvar>,
    val bekreftelser: Bekreftelser,
    val utbetalingsperioder: List<Utbetalingsperiode>,
    val andreUtbetalinger: List<AndreUtbetalinger>,
    val erArbeidstakerOgså: Boolean,
    val barn: List<Barn> = listOf(),
    val frilans: Frilans? = null,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende? = null,
    val vedlegg: List<URL> = listOf()
) {

    fun oppdaterBarnMedFnr(listeOverBarn: List<no.nav.omsorgspengerutbetaling.barn.Barn>) {
        barn.forEach { barn ->
            if (barn.manglerIdentitetsnummer()) {
                barn oppdaterIdentitetsnummerMed listeOverBarn.hentIdentitetsnummerForBarn(barn.aktørId)
            }
        }
    }

    fun tilKomplettSøknad(k9Format: Søknad, søker: Søker) = KomplettSøknad(
        søknadId = søknadId,
        språk = språk,
        mottatt = k9Format.mottattDato,
        søker = søker,
        harDekketTiFørsteDagerSelv = harDekketTiFørsteDagerSelv,
        bosteder = bosteder,
        opphold = opphold,
        spørsmål = spørsmål,
        utbetalingsperioder = utbetalingsperioder,
        andreUtbetalinger = andreUtbetalinger,
        vedleggId = vedlegg.map { it.vedleggId() },
        frilans = frilans,
        selvstendigNæringsdrivende = selvstendigNæringsdrivende,
        barn = barn,
        erArbeidstakerOgså = erArbeidstakerOgså,
        bekreftelser = bekreftelser,
        k9FormatSøknad = k9Format
    )
}

enum class Språk(@JsonValue val språk: String) {
    BOKMÅL("nb"),
    NYNORSK("nn");
}

fun URL.vedleggId() = this.toString().substringAfterLast("/")

fun List<no.nav.omsorgspengerutbetaling.barn.Barn>.hentIdentitetsnummerForBarn(aktørId: String?): String? {
    return firstOrNull() { it.aktørId == aktørId }?.identitetsnummer
}