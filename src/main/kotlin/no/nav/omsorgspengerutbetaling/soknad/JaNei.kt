package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

data class JaNei(
    val id: JaNeiId? = null,
    val spørsmål: String,
    val svar: JaNeiSvar
)

enum class JaNeiId {
    HarBekreftetOpplysninger,
    HarForståttRettigheterOgPlikter
}

typealias JaNeiSpørsmål = String

enum class JaNeiSvar {
    Ja,
    Nei,
}

internal fun List<JaNei>.valider() : Set<Violation> {
    val violations = mutableSetOf<Violation>()

    filter { it.spørsmål.isBlank() }.forEachIndexed { index, jaNei ->
        violations.add(
            Violation(
                parameterName = "jaNei[$index].spørsmål",
                parameterType = ParameterType.ENTITY,
                reason = "Spørsmål må være satt",
                invalidValue = jaNei.spørsmål
            )
        )
    }

    JaNeiId.values().forEach { id ->
        if (find { id == it.id } == null) {
            violations.add(
                Violation(
                    parameterName = "jaNei",
                    parameterType = ParameterType.ENTITY,
                    reason = "Spørsmål med id $id må besvares for å sende inn søknaden.",
                    invalidValue = null
                )
            )
        }
    }


    filter { it.id != null }.filter { it.svar == JaNeiSvar.Nei }.forEachIndexed { index, jaNei ->
        violations.add(
            Violation(
                parameterName = "jaNei[$index].svar",
                parameterType = ParameterType.ENTITY,
                reason = "Spørsmål med id ${jaNei.id} må besvares Ja for å sende inn søknad.",
                invalidValue = JaNeiSvar.Nei
            )
        )
    }

    return violations
}