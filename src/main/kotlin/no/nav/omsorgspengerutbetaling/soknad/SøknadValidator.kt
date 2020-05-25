package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.*
import java.time.format.DateTimeFormatter

private const val MAX_FRITEKST_TEGN = 1000
internal val vekttallProviderFnr1: (Int) -> Int = { arrayOf(3, 7, 6, 1, 8, 9, 4, 5, 2).reversedArray()[it] }
internal val vekttallProviderFnr2: (Int) -> Int = { arrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2).reversedArray()[it] }
private val fnrDateFormat = DateTimeFormatter.ofPattern("ddMMyy")

internal fun Søknad.valider() {
    val violations = mutableSetOf<Violation>().apply {
        addAll(utbetalingsperioder.valider())
        andreUtbetalinger?.let { addAll(it.valider()) } // TODO: Fjen optional når prodsatt.
        addAll(opphold.valider("opphold"))
        addAll(bosteder.valider("bosteder"))
        addAll(spørsmål.valider())
        addAll(bekreftelser.valider())
        addAll(validerInntektsopplysninger())
        addAll(validerSelvstendigVirksomheter(selvstendigVirksomheter))
        fosterbarn?.let { addAll(validerFosterbarn(it))}
        endringArbeidssituasjon?.let { addAll(it.valider())}
    }

    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
    }
}

private fun validerFosterbarn(fosterbarn: List<FosterBarn>) = mutableSetOf<Violation>().apply {
    fosterbarn.mapIndexed { index, barn ->
        if (!barn.fødselsnummer.erGyldigNorskIdentifikator()) {
            add(
                Violation(
                    parameterName = "fosterbarn[$index].fødselsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ikke gyldig fødselsnummer.",
                    invalidValue = barn.fødselsnummer
                )
            )
        }
    }
}

private fun Søknad.validerInntektsopplysninger() = mutableSetOf<Violation>().apply {
    if (frilans == null && selvstendigVirksomheter.isEmpty()) {
        add(
            Violation(
                parameterName = "frilans/selvstendigVirksomheter",
                parameterType = ParameterType.ENTITY,
                reason = "Må settes 'frilans' eller minst en 'selvstendigVirksomheter'",
                invalidValue = null
            )
        )
    }
}

private fun validerSelvstendigVirksomheter(
    selvstendigVirksomheter: List<Virksomhet>
): MutableSet<Violation> = mutableSetOf<Violation>().apply {
    if (selvstendigVirksomheter.isNotEmpty()) {
        selvstendigVirksomheter.forEach { virksomhet ->
            addAll(virksomhet.validate())
        }
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
