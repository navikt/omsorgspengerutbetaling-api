package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.Duration
import java.time.LocalDate

private const val JsonPath = "utbetalingsperioder"

enum class FraværÅrsak {
    STENGT_SKOLE_ELLER_BARNEHAGE,
    SMITTEVERNHENSYN,
    ORDINÆRT_FRAVÆR,
}

enum class AktivitetFravær {
    FRILANSER,
    SELVSTENDIG_VIRKSOMHET
}

internal fun Utbetalingsperiode.somPeriode() = Periode(
    fraOgMed = fraOgMed,
    tilOgMed = tilOgMed
)

data class Utbetalingsperiode(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val lengde: Duration? = null, //TODO: Fjerne etter prodsetting
    val antallTimerBorte: Duration? = null,
    val antallTimerPlanlagt: Duration? = null,
    val årsak: FraværÅrsak? = null, // TODO: 15/03/2021 Fjern nullable etter prodsetting.
    val aktivitetFravær: List<AktivitetFravær> = listOf()
)

internal fun List<Utbetalingsperiode>.valider() : Set<Violation> {
    val violations = mutableSetOf<Violation>()

    if (isEmpty()) {
        violations.add(
            Violation(
                parameterName = JsonPath,
                parameterType = ParameterType.ENTITY,
                reason = "Må settes minst en utbetalingsperiode.",
                invalidValue = this
            )
        )
    }

    val perioder = map { it.somPeriode() }
    violations.addAll(perioder.valider(JsonPath))

    mapIndexed { utbetalingsperiodeIndex, utbetalingsperiode ->
        if(utbetalingsperiode.antallTimerPlanlagt != null && utbetalingsperiode.antallTimerBorte == null){
            violations.add(
                Violation(
                    parameterName = "${JsonPath}[$utbetalingsperiodeIndex]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Dersom antallTimerPlanlagt er satt så kan ikke antallTimerBorte være tom",
                    invalidValue = "antallTimerBorte = ${utbetalingsperiode.antallTimerBorte}, antallTimerPlanlagt=${utbetalingsperiode.antallTimerPlanlagt}"
                )
            )
        }

        if(utbetalingsperiode.antallTimerBorte != null && utbetalingsperiode.antallTimerPlanlagt == null){
            violations.add(
                Violation(
                    parameterName = "${JsonPath}[$utbetalingsperiodeIndex]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Dersom antallTimerBorte er satt så kan ikke antallTimerPlanlagt være tom",
                    invalidValue = "antallTimerBorte = ${utbetalingsperiode.antallTimerBorte}, antallTimerPlanlagt=${utbetalingsperiode.antallTimerPlanlagt}"
                )
            )
        }

        if(utbetalingsperiode.antallTimerBorte != null && utbetalingsperiode.antallTimerPlanlagt != null){
            if(utbetalingsperiode.antallTimerBorte > utbetalingsperiode.antallTimerPlanlagt){
                violations.add(
                    Violation(
                        parameterName = "${JsonPath}[$utbetalingsperiodeIndex]",
                        parameterType = ParameterType.ENTITY,
                        reason = "Antall timer borte kan ikke være større enn antall timer planlagt jobbe",
                        invalidValue = "antallTimerBorte = ${utbetalingsperiode.antallTimerBorte}, antallTimerPlanlagt=${utbetalingsperiode.antallTimerPlanlagt}"
                    )
                )
            }
        }
    }
    return violations
}
