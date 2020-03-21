package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

data class Bosted(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String,
    val erEØSLand: JaNei
)

typealias Opphold = Bosted

internal fun List<Bosted>.valider(jsonPath: String) : Set<Violation> {
    val violations = mutableSetOf<Violation>()
    val perioder = map { Periode(fraOgMed = it.fraOgMed, tilOgMed = it.tilOgMed) }
    violations.addAll(perioder.valider(jsonPath))

    forEachIndexed { index, it ->
        if (it.landkode.isBlank()) {
            violations.add(
                Violation(
                    parameterName = "$jsonPath[$index].landkode",
                    parameterType = ParameterType.ENTITY,
                    reason = "Landkode må settes",
                    invalidValue = it.landkode
                )
            )
        }
        if (it.landnavn.isBlank()) {
            violations.add(
                Violation(
                    parameterName = "$jsonPath[$index].landnavn",
                    parameterType = ParameterType.ENTITY,
                    reason = "Landnavn må settes",
                    invalidValue = it.landkode
                )
            )
        }
    }
    return violations
}
