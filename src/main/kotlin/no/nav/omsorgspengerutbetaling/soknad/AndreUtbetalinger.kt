package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

const val DAGPENGER = "dagpenger"
const val SYKEPENGER = "sykepenger"
const val MIDLERTIDIG_KOMPENSASJON_SN_FRI = "midlertidigkompensasjonsnfri"

fun List<String>.valider(): Set<Violation> {
    val violations = mutableSetOf<Violation>()

    mapIndexed { index, annenUtbetaling ->
        if (annenUtbetaling != DAGPENGER && annenUtbetaling != SYKEPENGER && annenUtbetaling != MIDLERTIDIG_KOMPENSASJON_SN_FRI) {
            violations.add(
                Violation(
                    parameterName = "andreUtbetalinger[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ugyldig verdi for annen utbetaling. Kun 'dagpenger', 'sykepenger' og 'midlertidigkompensasjonsnfri' er tillatt.",
                    invalidValue = annenUtbetaling
                )
            )
        }
    }

    return violations
}
