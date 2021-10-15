package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

data class Frilans(
    @JsonFormat(pattern = "yyyy-MM-dd") val startdato: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val sluttdato: LocalDate? = null,
    val jobberFortsattSomFrilans: JaNei
)

fun Frilans.valider(): MutableSet<Violation> {
    val feil = mutableSetOf<Violation>()

    if (sluttdato != null && sluttdato.isBefore(startdato)) {
        feil.add(
            Violation(
                parameterName = "frilans.sluttdato",
                parameterType = ParameterType.ENTITY,
                reason = "Sluttdato kan ikke være før startdato",
                invalidValue = "startdato=$startdato, sluttdato=$sluttdato"
            )
        )
    }

    if(sluttdato == null && jobberFortsattSomFrilans==JaNei.Nei) {
        feil.add(
            Violation(
                parameterName = "frilans.sluttdato",
                parameterType = ParameterType.ENTITY,
                reason = "Sluttdato kan ikke være null dersom jobberFortsattSomFrilans=Nei",
                invalidValue = "sluttdato=$sluttdato,jobberFortsattSomFrilans=$jobberFortsattSomFrilans "
            )
        )
    }

    return feil
}

