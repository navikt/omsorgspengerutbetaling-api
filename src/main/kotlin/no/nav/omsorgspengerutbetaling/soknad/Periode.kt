package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

internal data class Periode (
    internal val fraOgMed: LocalDate,
    internal val tilOgMed: LocalDate
)

// TODO: Godta overlapp?
internal fun List<Periode>.valider(jsonPath: String) : Set<Violation> {
    val violations = mutableSetOf<Violation>()
    mapIndexed { index, periode ->
        if (periode.fraOgMed.isAfter(periode.tilOgMed)) {
            violations.add(
                Violation(
                    parameterName = "$jsonPath[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Til og med må være etter eller lik fra og med",
                    invalidValue = periode.tilOgMed
                )
            )
        }
    }
    return violations
}