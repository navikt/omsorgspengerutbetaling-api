package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.*

private const val MAX_FRITEKST_TEGN = 1000

internal fun Søknad.valider() {
    val violations = mutableSetOf<Violation>().apply {
        addAll(utbetalingsperioder.valider())
        addAll(opphold.valider("opphold"))
        addAll(bosteder.valider("bosteder"))
        addAll(spørsmål.valider())
        addAll(validerFrilans(frilans, harHattInntektSomFrilanser))
        addAll(validerSelvstendigVirksomheter(selvstendigVirksomheter, harHattInntektSomSelvstendigNaringsdrivende))

    }

    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
    }
}

private fun validerFrilans(frilans: Frilans?, harHattInntektSomFrilanser: Boolean): MutableSet<Violation> =
    mutableSetOf<Violation>().apply {
        if (harHattInntektSomFrilanser) {
            if (frilans == null) {
                add(
                    Violation(
                        parameterName = "harHattInntektSomFrilanser",
                        parameterType = ParameterType.ENTITY,
                        reason = "Dersom søkeren har hatt inntekter som frilanser, skal frilans objektet ikke være null"
                    )
                )
            }
        }

        if (!harHattInntektSomFrilanser) {
            if (frilans != null) {
                add(
                    Violation(
                        parameterName = "harHattInntektSomFrilanser",
                        parameterType = ParameterType.ENTITY,
                        reason = "Dersom søkeren IKKE har hatt inntekter som frilanser, skal frilans objektet være null"
                    )
                )
            }
        }
    }

private fun validerSelvstendigVirksomheter(
    selvstendigVirksomheter: List<Virksomhet>?,
    harHattInntektSomSelvstendigNaringsdrivende: Boolean
): MutableSet<Violation> = mutableSetOf<Violation>().apply {
    if (harHattInntektSomSelvstendigNaringsdrivende) {
        if (selvstendigVirksomheter != null && selvstendigVirksomheter.isNotEmpty()) {
            selvstendigVirksomheter.forEach { virksomhet ->
                addAll(virksomhet.validate())
            }
        }
    }

    if (harHattInntektSomSelvstendigNaringsdrivende) {
        if (selvstendigVirksomheter != null && selvstendigVirksomheter.isEmpty()) {
            add(
                Violation(
                    parameterName = "harHattInntektSomSelvstendigNaringsdrivende",
                    parameterType = ParameterType.ENTITY,
                    reason = "Hvis harHattInntektSomSelvstendigNaringsdrivende er true så kan ikke listen over virksomehter være tom",
                    invalidValue = harHattInntektSomSelvstendigNaringsdrivende
                )
            )
        }
    }
}


internal fun String.erBlankEllerForLangFritekst(): Boolean = isBlank() || length > MAX_FRITEKST_TEGN
