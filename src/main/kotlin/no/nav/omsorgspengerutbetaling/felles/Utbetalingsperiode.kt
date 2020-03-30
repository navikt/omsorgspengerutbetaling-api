package no.nav.omsorgspengerutbetaling.felles

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.omsorgspengerutbetaling.vedlegg.Vedlegg
import java.net.URI
import java.net.URL
import java.time.Duration
import java.time.LocalDate

private object Verktøy{
    internal const val MAX_VEDLEGG_SIZE = 24 * 1024 * 1024

    internal const val JsonPath = "utbetalingsperioder"

    internal val VedleggUrlRegex = Regex("/vedlegg/.*")

    internal val VedleggTooLargeProblemDetails = DefaultProblemDetails(
        title = "attachments-too-large",
        status = 413,
        detail = "Totale størreslsen på alle vedlegg overstiger maks på 24 MB."
    )
}

data class UtbetalingsperiodeMedVedlegg(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val lengde: Duration? = null,
    val legeerklæringer: List<URI> = listOf()
)

internal fun UtbetalingsperiodeMedVedlegg.somPeriode() =
    Periode(
        fraOgMed = fraOgMed,
        tilOgMed = tilOgMed
    )

data class UtbetalingsperiodeUtenVedlegg(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val lengde: Duration?
)

internal fun List<UtbetalingsperiodeMedVedlegg>.valider() : Set<Violation> {
    val violations = mutableSetOf<Violation>()

    if (isEmpty()) {
        violations.add(
            Violation(
                parameterName = Verktøy.JsonPath,
                parameterType = ParameterType.ENTITY,
                reason = "Må settes minst en utbetalingsperiode.",
                invalidValue = this
            )
        )
    }

    val perioder = map { it.somPeriode() }
    violations.addAll(perioder.valider(Verktøy.JsonPath))

    mapIndexed { utbetalingsperiodeIndex, utbetalingsperiode ->
        utbetalingsperiode.legeerklæringer.mapIndexed { legeærklæringIndex, uri ->
            // Kan oppstå uri = null etter Jackson deserialisering
            if (uri == null || !uri.path.matches(Verktøy.VedleggUrlRegex)) {
                violations.add(
                    Violation(
                        parameterName = "${Verktøy.JsonPath}[$utbetalingsperiodeIndex].legeerklæringer[$legeærklæringIndex]",
                        parameterType = ParameterType.ENTITY,
                        reason = "Ikke gyldig vedlegg URL.",
                        invalidValue = uri
                    )
                )
            }
        }
    }
    return violations
}

internal fun List<Vedlegg>.valider(vedleggReferanser: List<URL>) {

    if (vedleggReferanser.size != size) {
        throw Throwblem(
            ValidationProblemDetails(
                violations = setOf(
                    Violation(
                        parameterName = Verktøy.JsonPath,
                        parameterType = ParameterType.ENTITY,
                        reason = "Mottok referanse til ${vedleggReferanser.size} vedlegg, men fant kun $size vedlegg.",
                        invalidValue = vedleggReferanser
                    )
                )
            )
        )
    }

    val totalSize = sumBy { it.content.size }

    if (totalSize > Verktøy.MAX_VEDLEGG_SIZE) {
        throw Throwblem(Verktøy.VedleggTooLargeProblemDetails)
    }
}
