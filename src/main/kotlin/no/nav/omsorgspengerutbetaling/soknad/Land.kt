package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.util.*

/**
 * ISO 3166 alpha-3 landkode.
 *
 * @see https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3
 */

private val LANSKODER: MutableSet<String> = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3)

data class Land(val landkode: String, val landnavn: String)

internal fun Land.valider(feltnavn: String) = mutableSetOf<Violation>().apply {
    if (landkode.isBlank()) {
        add(
            Violation(
                parameterName = "${feltnavn}.landkode",
                parameterType = ParameterType.ENTITY,
                reason = "Landkode kan ikke være blank.",
                invalidValue = landkode
            )
        )
    }

    if (!LANSKODER.contains(landkode)) {
        add(
            Violation(
                parameterName = "${feltnavn}.landkode",
                parameterType = ParameterType.ENTITY,
                reason = "Landkode er ikke en gyldig ISO 3166-1 alpha-3 kode.",
                invalidValue = landkode
            )
        )
    }

    if (landnavn.isBlank()) {
        add(
            Violation(
                parameterName = "${feltnavn}.landnavn",
                parameterType = ParameterType.ENTITY,
                reason = "Landnavn kan ikke være blank.",
                invalidValue = landnavn
            )
        )
    }
}
