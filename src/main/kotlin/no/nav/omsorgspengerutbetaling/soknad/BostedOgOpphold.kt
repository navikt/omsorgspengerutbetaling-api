package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonFormat
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

internal fun List<Bosted>.valider(jsonPath: String): Set<Violation> {
    val violations = mutableSetOf<Violation>()

    val perioder = map { Periode(fraOgMed = it.fraOgMed, tilOgMed = it.tilOgMed) }
    violations.addAll(perioder.valider(jsonPath))

    val land = map { Land(it.landkode, it.landnavn) }
    land.forEachIndexed { index, land ->
        violations.addAll(land.valider("$jsonPath[$index]"))
    }

    return violations
}