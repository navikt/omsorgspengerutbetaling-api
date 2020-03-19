package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

data class SpørsmålOgSvar(
    val id: SpørsmålId? = null,
    val spørsmål: Spørsmål,
    val svar: Svar,
    val fritekst: Fritekst? = null
)

/**
 * Trenger bare lage ID'er på ting vi eksplisitt
 * må gjøre noen sjekker på eller vi trenger å ha
 * Et eksplisitt forhold til senere i prosesseringen.
 * Default bør være at det ikke er noen ID.
 */
enum class SpørsmålId {
    HarBekreftetOpplysninger,
    HarForståttRettigheterOgPlikter
}

typealias Spørsmål = String
typealias Fritekst = String

enum class Svar {
    Ja,
    Nei,
    VetIkke
}

internal fun List<SpørsmålOgSvar>.valider() : Set<Violation> {
    val violations = mutableSetOf<Violation>()

    filter { it.spørsmål.erBlankEllerForLangFritekst() }.forEachIndexed { index, spm ->
        violations.add(
            Violation(
                parameterName = "spørsmål[$index].spørsmål",
                parameterType = ParameterType.ENTITY,
                reason = "Spørsmål må være satt og være maks 1000 tegn.",
                invalidValue = spm.spørsmål
            )
        )
    }

    filter {it.fritekst != null}.filter { it.fritekst!!.erBlankEllerForLangFritekst() }.forEachIndexed { index, spm ->
        violations.add(
            Violation(
                parameterName = "spørsmål[$index].spørsmål",
                parameterType = ParameterType.ENTITY,
                reason = "Fritekst kan ikke være tom og kan maks være 1000 tegn.",
                invalidValue = spm.fritekst
            )
        )
    }

    SpørsmålId.values().forEach { id ->
        if (find { id == it.id } == null) {
            violations.add(
                Violation(
                    parameterName = "spørsmål",
                    parameterType = ParameterType.ENTITY,
                    reason = "Spørsmål med id $id må besvares for å sende inn søknaden.",
                    invalidValue = null
                )
            )
        }
    }

    filter { it.id != null }.filter { it.svar == Svar.Nei }.forEachIndexed { index, spm ->
        violations.add(
            Violation(
                parameterName = "spørsmål[$index].svar",
                parameterType = ParameterType.ENTITY,
                reason = "Spørsmål med id ${spm.id} må besvares Ja for å sende inn søknad.",
                invalidValue = Svar.Nei
            )
        )
    }

    return violations
}