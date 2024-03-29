package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetaling
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetalingValidator
import no.nav.omsorgspengerutbetaling.vedlegg.Vedlegg
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val MAX_FRITEKST_TEGN = 1000
private const val MAX_VEDLEGG_SIZE = 24 * 1024 * 1024
internal val vekttallProviderFnr1: (Int) -> Int = { arrayOf(3, 7, 6, 1, 8, 9, 4, 5, 2).reversedArray()[it] }
internal val vekttallProviderFnr2: (Int) -> Int = { arrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2).reversedArray()[it] }
private val fnrDateFormat = DateTimeFormatter.ofPattern("ddMMyy")

internal val VedleggUrlRegex = Regex("/vedlegg/.*")

internal val VedleggTooLargeProblemDetails = DefaultProblemDetails(
    title = "attachments-too-large",
    status = 413,
    detail = "Totale størreslsen på alle vedlegg overstiger maks på 24 MB."
)

internal fun Søknad.valider() {
    val violations = mutableSetOf<Violation>().apply {
        addAll(validerHarDekketTiFørsteDagerSelv())
        addAll(validerUtvidetRett())
        addAll(utbetalingsperioder.valider())
        addAll(opphold.valider("opphold"))
        addAll(bosteder.valider("bosteder"))
        addAll(spørsmål.valider())
        addAll(bekreftelser.valider())
        addAll(validerInntektsopplysninger())
        addAll(barn.validerBarn())
        frilans?.let { addAll(it.valider()) }
        selvstendigNæringsdrivende?.let { addAll(selvstendigNæringsdrivende.validate()) }
    }.sortedBy { it.reason }.toSet()

    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
    }
}


internal fun Barn.alder() = LocalDate.now().year.minus(this.fødselsdato.year)

private fun Søknad.validerUtvidetRett() = mutableSetOf<Violation>().apply {
    if(barn.isNotEmpty() && barn.all { it.alder() >= 13 }){
        if(barn.none { it.utvidetRett == true }){
            add(
                Violation(
                    parameterName = "barn[?].utvidetRett",
                    parameterType = ParameterType.ENTITY,
                    reason = "Hvis alle barn er 13 år eller eldre må minst et barn ha utvidet rett"
                )
            )
        }
    }
}

private fun Søknad.validerHarDekketTiFørsteDagerSelv() = mutableSetOf<Violation>().apply {
    if (barn.any { it.alder() <= 12 }) {
        if (harDekketTiFørsteDagerSelv != true) {
            add(
                Violation(
                    parameterName = "harDekketTiFørsteDagerSelv",
                    parameterType = ParameterType.ENTITY,
                    reason = "harDekketTiFørsteDagerSelv må være true dersom et barn er 12 år eller yngre.",
                    invalidValue = harDekketTiFørsteDagerSelv
                )
            )
        }
    }
}

fun no.nav.k9.søknad.Søknad.valider(){
   val feil = OmsorgspengerUtbetalingValidator().valider(getYtelse<OmsorgspengerUtbetaling>()).map {
        Violation(
            parameterName = it.felt,
            parameterType = ParameterType.ENTITY,
            reason = it.feilmelding,
            invalidValue = "k9-format feilkode: ${it.feilkode}"
        )
    }.toSet()

    if (feil.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(feil))
    }
}

private fun List<Barn>.validerBarn(): MutableSet<Violation> {
    val feil = mutableSetOf<Violation>()

    this.forEachIndexed { index, barn ->
        if(barn.identitetsnummer == null){
            feil.add(
                Violation(
                    parameterName = "barn[$index].identitetsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "identitetsnummer må være satt"
                )
            )
        }

        if(barn.identitetsnummer != null && !barn.identitetsnummer!!.erGyldigFodselsnummer()){
            feil.add(
                Violation(
                    parameterName = "barn[$index].identitetsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ugyldig identitetsnummer"
                )
            )
        }
    }

    return feil
}

private fun Søknad.validerInntektsopplysninger() = mutableSetOf<Violation>().apply {
    if (frilans == null && selvstendigNæringsdrivende == null) {
        add(
            Violation(
                parameterName = "frilans/selvstendigNæringsdrivende",
                parameterType = ParameterType.ENTITY,
                reason = "Må settes 'frilans' eller 'selvstendigNæringsdrivende'",
                invalidValue = null
            )
        )
    }
}

internal fun String.erBlankEllerForLangFritekst(): Boolean = isBlank() || length > MAX_FRITEKST_TEGN


fun String.erGyldigNorskIdentifikator(): Boolean {
    if (length != 11 || !erKunSiffer() || !starterMedFodselsdato()) return false

    val forventetKontrollsifferEn = get(9)

    val kalkulertKontrollsifferEn = Mod11.kontrollsiffer(
        number = substring(0, 9),
        vekttallProvider = vekttallProviderFnr1
    )

    if (kalkulertKontrollsifferEn != forventetKontrollsifferEn) return false

    val forventetKontrollsifferTo = get(10)

    val kalkulertKontrollsifferTo = Mod11.kontrollsiffer(
        number = substring(0, 10),
        vekttallProvider = vekttallProviderFnr2
    )

    return kalkulertKontrollsifferTo == forventetKontrollsifferTo
}

fun String.starterMedFodselsdato(): Boolean {
    // Sjekker ikke hvilket århundre vi skal tolket yy som, kun at det er en gyldig dato.
    // F.eks blir 290990 parset til 2090-09-29, selv om 1990-09-29 var ønskelig.
    // Kunne sett på individsifre (Tre første av personnummer) for å tolke århundre,
    // men virker unødvendig komplekst og sårbart for ev. endringer i fødselsnummeret.
    return try {
        var substring = substring(0, 6)
        val førsteSiffer = (substring[0]).toString().toInt()
        if (førsteSiffer in 4..7) {
            substring = (førsteSiffer - 4).toString() + substring(1, 6)
        }
        fnrDateFormat.parse(substring)

        true
    } catch (cause: Throwable) {
        false
    }
}

/**
 * https://github.com/navikt/helse-sparkel/blob/2e79217ae00632efdd0d4e68655ada3d7938c4b6/src/main/kotlin/no/nav/helse/ws/organisasjon/Mod11.kt
 * https://www.miles.no/blogg/tema/teknisk/validering-av-norske-data
 */
internal object Mod11 {
    private val defaultVekttallProvider: (Int) -> Int = { 2 + it % 6 }

    internal fun kontrollsiffer(
        number: String,
        vekttallProvider: (Int) -> Int = defaultVekttallProvider
    ): Char {
        return number.reversed().mapIndexed { i, char ->
            Character.getNumericValue(char) * vekttallProvider(i)
        }.sum().let(Mod11::kontrollsifferFraSum)
    }


    private fun kontrollsifferFraSum(sum: Int) = sum.rem(11).let { rest ->
        when (rest) {
            0 -> '0'
            1 -> '-'
            else -> "${11 - rest}"[0]
        }
    }
}


internal fun List<Vedlegg>.valider(vedleggReferanser: List<URL>) {

    if (vedleggReferanser.size != size) {
        throw Throwblem(
            ValidationProblemDetails(
                violations = setOf(
                    Violation(
                        parameterName = "vedlegg",
                        parameterType = ParameterType.ENTITY,
                        reason = "Mottok referanse til ${vedleggReferanser.size} vedlegg, men fant kun $size vedlegg.",
                        invalidValue = vedleggReferanser
                    )
                )
            )
        )
    }

    val totalSize = sumOf { it.content.size }

    if (totalSize > MAX_VEDLEGG_SIZE) {
        throw Throwblem(VedleggTooLargeProblemDetails)
    }
}
