package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.*

private const val MAX_FRITEKST_TEGN = 1000

internal fun Søknad.valider() {
    val violations = mutableSetOf<Violation>().apply {
        addAll(utbetalingsperioder.valider())
        addAll(opphold.valider("opphold"))
        addAll(bosteder.valider("bosteder"))
        addAll(spørsmål.valider())
    }

    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
    }
}

internal fun String.erBlankEllerForLangFritekst(): Boolean = isBlank() || length > MAX_FRITEKST_TEGN
