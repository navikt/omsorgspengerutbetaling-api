package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.*

private const val MAX_FRITEKST_TEGN = 1000

internal fun Søknad.valider() {
    val violations = mutableSetOf<Violation>().apply {
        addAll(utbetalingsperioder.valider())
        addAll(opphold.valider("opphold"))
        addAll(bosteder.valider("bosteder"))
        addAll(spørsmål.valider())
        addAll(bekreftelser.valider())
        addAll(validerInntektsopplysninger())
        addAll(validerSelvstendigVirksomheter(selvstendigVirksomheter))
        //TODO: Valider fosterbarn
    }

    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
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
