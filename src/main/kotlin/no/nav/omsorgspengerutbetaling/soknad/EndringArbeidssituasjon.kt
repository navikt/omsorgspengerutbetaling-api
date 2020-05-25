package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

data class EndringArbeidssituasjon(
    val harEndringFrilans: JaNei,
    val endringerFrilans: List<Endring> = listOf(),
    val harEndringSelvstendig: JaNei,
    val endringerSelvstendig: List<Endring> = listOf()
)

data class Endring(
    val dato: LocalDate,
    val forklaring: String
)


internal fun EndringArbeidssituasjon.valider(): MutableSet<Violation>{
    val violations = mutableSetOf<Violation>()

    if(harEndringFrilans == JaNei.Ja && endringerFrilans.isEmpty()){
        violations.add(
            Violation(
                parameterName = "endringerFrilans",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis harEndringFrilans er ja/true så må endringerFrilans inneholde minst en endring",
                invalidValue = endringerFrilans
            )
        )
    }

    if(harEndringSelvstendig == JaNei.Ja && endringerSelvstendig.isEmpty()){
        violations.add(
            Violation(
                parameterName = "endringerSelvstendig",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis harEndringSelvstendig er ja/true så må endringerSelvstendig inneholde minst en endring",
                invalidValue = endringerSelvstendig
            )
        )
    }

    return violations
}