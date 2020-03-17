package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgspengerutbetaling.vedlegg.Vedlegg
import java.net.URI
import java.time.Duration
import java.time.LocalDate

private object Verktøy{
    internal const val MAX_VEDLEGG_SIZE = 24 * 1024 * 1024

    internal const val JsonPath = "utbetalingsplaner"

    internal val VedleggUrlRegex = Regex("/vedlegg/.*")

    internal val VedleggTooLargeProblemDetails = DefaultProblemDetails(
        title = "attachments-too-large",
        status = 413,
        detail = "Totale størreslsen på alle vedlegg overstiger maks på 24 MB."
    )
}

data class Utbetalingsperiode<VedleggType>(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val lengde: Duration?,
    val legeærklæringer: List<VedleggType>
)

typealias UtbetalingsperiodeUri = Utbetalingsperiode<URI>
typealias UtbetalingsperiodeVedlegg = Utbetalingsperiode<Vedlegg>


internal fun List<UtbetalingsperiodeUri>.valider() : Set<Violation> {
    val violations = mutableSetOf<Violation>()

    val perioder = map { Periode(fraOgMed = it.fraOgMed, tilOgMed = it.tilOgMed) }
    violations.addAll(perioder.valider(Verktøy.JsonPath))

    mapIndexed { utbetalingsperiodeIndex, utbetalingsperiode ->
        utbetalingsperiode.legeærklæringer.mapIndexed { legeærklæringIndex, uri ->
            // Kan oppstå uri = null etter Jackson deserialisering
            if (uri == null || !uri.path.matches(Verktøy.VedleggUrlRegex)) {
                violations.add(
                    Violation(
                        parameterName = "${Verktøy.JsonPath}[$utbetalingsperiodeIndex].legerklæringer[$legeærklæringIndex]",
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

internal fun List<UtbetalingsperiodeVedlegg>.valider() {
    val totalSize = map { periode ->
        periode.legeærklæringer.sumBy { legeærklæring ->
            legeærklæring.content.size
        }
    }.sum()

    if (totalSize > Verktøy.MAX_VEDLEGG_SIZE) {
        throw Throwblem(Verktøy.VedleggTooLargeProblemDetails)
    }
}