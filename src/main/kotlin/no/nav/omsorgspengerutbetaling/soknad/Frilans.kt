package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

data class  Frilans(
    @JsonFormat(pattern = "yyyy-MM-dd") val startdato: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val sluttdato: LocalDate? = null,
    val jobberFortsattSomFrilans: JaNei
) {

    fun valider(): MutableSet<Violation> = mutableSetOf<Violation>().apply {
        if (jobberFortsattSomFrilans == JaNei.Nei && sluttdato == null) {
            add(
                Violation(
                    parameterName = "frilans.sluttdato",
                    parameterType = ParameterType.ENTITY,
                    reason = "Sluttdato kan ikke være null dersom jobberFortsattSomFrilans er false.",
                    invalidValue = sluttdato
                )
            )
        }

        if (sluttdato != null && startdato.isAfter(sluttdato)) {
            add(
                Violation(
                    parameterName = "frilans.startdato",
                    parameterType = ParameterType.ENTITY,
                    reason = "Startdato kan ikke være etter sluttdato",
                    invalidValue = startdato
                )
            )
        }
    }
}

