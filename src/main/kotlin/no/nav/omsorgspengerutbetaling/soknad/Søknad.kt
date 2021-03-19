package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonValue
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.omsorgspengerutbetaling.barn.BarnOppslag
import java.net.URL
import java.util.*

data class Søknad(
    val søknadId: SøknadId = SøknadId(UUID.randomUUID().toString()),
    val språk: Språk,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val spørsmål: List<SpørsmålOgSvar>,
    val bekreftelser: Bekreftelser,
    val utbetalingsperioder: List<UtbetalingsperiodeMedVedlegg>,
    val andreUtbetalinger: List<String>?, //TODO: Fjern ? når dette er prodsatt.
    val erArbeidstakerOgså: Boolean,
    val fosterbarn: List<FosterBarn>? = listOf(),
    val barn: List<Barn> = listOf(),
    val frilans: Frilans? = null,
    val selvstendigVirksomheter: List<Virksomhet> = listOf(),
    val vedlegg: List<URL>? = listOf(),
    val hjemmePgaSmittevernhensyn: Boolean? = null, // TODO: 15/03/2021 utgår
    val hjemmePgaStengtBhgSkole: Boolean? = null // TODO: 15/03/2021 utgår
) {
    fun oppdaterBarnMedFnr(listeOverBarnOppslag: List<BarnOppslag>) {
        barn.forEach { barn ->
            if (barn.manglerIdentitetsnummer()) {
                barn oppdaterIdentitetsnummerMed listeOverBarnOppslag.hentIdentitetsnummerForBarn(barn.aktørId)
            }
        }
    }
}

enum class Språk(@JsonValue val språk: String) {
    BOKMÅL("nb"),
    NYNORSK("nn");
}

private fun List<BarnOppslag>.hentIdentitetsnummerForBarn(aktørId: String?): String? {
    return find {
        it.aktørId == aktørId
    }?.identitetsnummer
}
