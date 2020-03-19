package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private object PeriodeVerktøy {
    internal val formatter = DateTimeFormatter.ofPattern("dd.MM.yy")
}

internal data class Periode (
    internal val fraOgMed: LocalDate,
    internal val tilOgMed: LocalDate
) {
    private val display = if (fraOgMed.isEqual(tilOgMed)) PeriodeVerktøy.formatter.format(fraOgMed) else {
        "${PeriodeVerktøy.formatter.format(fraOgMed)}-${PeriodeVerktøy.formatter.format(tilOgMed)}"
    }
    override fun toString(): String = display
}

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