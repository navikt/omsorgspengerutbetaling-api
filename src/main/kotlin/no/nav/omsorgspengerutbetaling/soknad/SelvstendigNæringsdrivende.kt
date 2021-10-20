package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

data class SelvstendigNæringsdrivende(
    val næringstyper: List<Næringstyper> = listOf(),
    val fiskerErPåBladB: JaNei = JaNei.Nei,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate? = null,
    val næringsinntekt: Int? = null,
    val navnPåVirksomheten: String,
    val organisasjonsnummer: String? = null,
    val registrertINorge: JaNei,
    val registrertIUtlandet: Land? = null,
    val yrkesaktivSisteTreFerdigliknedeÅrene: YrkesaktivSisteTreFerdigliknedeArene? = null,
    val varigEndring: VarigEndring? = null,
    val regnskapsfører: Regnskapsfører? = null,
    val erNyoppstartet: Boolean,
    val harFlereAktiveVirksomheter: Boolean? = null
)

data class YrkesaktivSisteTreFerdigliknedeArene(
    val oppstartsdato: LocalDate
)

enum class Næringstyper {
    FISKE,
    JORDBRUK_SKOGBRUK,
    DAGMAMMA,
    ANNEN
}

data class VarigEndring(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dato: LocalDate,
    val inntektEtterEndring: Int,
    val forklaring: String
)

data class Regnskapsfører(
    val navn: String,
    val telefon: String
)


internal fun SelvstendigNæringsdrivende.validate(): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    // TODO: 18/10/2021 Felt utgår når selvstendigNæringsdrivende er prodsatt
    val felt = "selvstendigNæringsdrivende"

    tilOgMed?.apply {
        violations.addAll(Periode(fraOgMed, tilOgMed).valider())
    }

    val fireÅrSiden = LocalDate.now().minusYears(4)
    if (erNyoppstartet && !fraOgMed.isAfter(fireÅrSiden)) {
        violations.add(
            Violation(
                parameterName = "${felt}.erNyoppstartet",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis erNyoppstartet er true så må fraOgMed være etter $fireÅrSiden",
                invalidValue = fraOgMed
            )
        )
    }
    if (!erNyoppstartet && fraOgMed.isAfter(fireÅrSiden)) {
        violations.add(
            Violation(
                parameterName = "${felt}.erNyoppstartet",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis erNyoppstartet er false så må fraOgMed være før $fireÅrSiden",
                invalidValue = fraOgMed
            )
        )
    }

    if(harFlereAktiveVirksomheter == null){
        violations.add(
            Violation(
                parameterName = "${felt}.harFlereAktiveVirksomheter",
                parameterType = ParameterType.ENTITY,
                reason = "harFlereAktiveVirksomheter må være satt til true eller false, ikke null",
                invalidValue = harFlereAktiveVirksomheter
            )
        )
    }

    return violations
}