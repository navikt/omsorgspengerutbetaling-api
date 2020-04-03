package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

const val DAGPENGER = "dagpenger"
const val SYKEPENGER = "sykepenger"

fun List<String>.valider(): Set<Violation> {
    val violations = mutableSetOf<Violation>()

    mapIndexed { index, annenUtbetaling ->
        if (annenUtbetaling != DAGPENGER && annenUtbetaling != SYKEPENGER) {
            violations.add(
                Violation(
                    parameterName = "andreUtbetalinger[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ugyldig verdig for andre utbetaling. Kun 'dagpenger' og 'sykepenger' er tillatt.",
                    invalidValue = annenUtbetaling
                )
            )
        }
    }

    return violations
}
