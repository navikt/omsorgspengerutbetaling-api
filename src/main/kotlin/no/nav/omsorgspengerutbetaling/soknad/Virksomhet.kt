package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

data class Virksomhet(
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
    val registrertILand: String? = null, //TODO: Kan fjernes etter at registrertIUtlandet er prodsatt og det har gått mer enn 24t.
    val registrertIUtlandet: Land? = null,
    val yrkesaktivSisteTreFerdigliknedeÅrene: YrkesaktivSisteTreFerdigliknedeArene? = null,
    val varigEndring: VarigEndring? = null,
    val regnskapsfører: Regnskapsfører? = null
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


internal fun Virksomhet.validate(index: Int): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()
    val felt = "selvstendigVirksomheter[$index]"

    tilOgMed?.apply {
        violations.addAll(Periode(fraOgMed, tilOgMed).valider())
    }

    when {
        erVirksomhetIUtlandet() -> {
            when {
                //TODO: Fjern case etter at frontend har vært prodatt i mer enn 24 timer.
                !erRegistrertILandGyldigSatt() -> {
                    violations.add(
                        Violation(
                            parameterName = "${felt}.registrertILand",
                            parameterType = ParameterType.ENTITY,
                            reason = "Hvis registrertINorge er false så må registrertILand være satt.",
                            invalidValue = registrertILand
                        )
                    )
                }
                erRegistrertIUtlLandetGyldigSatt() -> {
                        violations.addAll(registrertIUtlandet!!.valider("${felt}.registrertIUtlandet"))
                }
                //TODO: Aktiver dette når har frontend har vært prodsatt i mer enn 24t.
                /*!erRegistrertIUtlLandetGyldigSatt() -> {
                    violations.add(
                        Violation(
                            parameterName = "${felt}.registrertIUtlandet",
                            parameterType = ParameterType.ENTITY,
                            reason = "Hvis registrertINorge er false så må registrertIUtlandet være satt.",
                            invalidValue = registrertIUtlandet
                        )
                    )
                }*/
            }
        }
        erVirksomhetINorge() -> {
            if (!erRegistrertINorgeGyldigSatt()) {
                violations.add(
                    Violation(
                        parameterName = "${felt}.organisasjonsnummer",
                        parameterType = ParameterType.ENTITY,
                        reason = "Hvis registrertINorge er true så må også organisasjonsnummer være satt",
                        invalidValue = organisasjonsnummer
                    )
                )
            }
        }
    }
    return violations
}

private fun Virksomhet.erRegistrertINorgeGyldigSatt(): Boolean {
    return !organisasjonsnummer.isNullOrBlank()
}

private fun Virksomhet.erRegistrertILandGyldigSatt(): Boolean = !registrertILand.isNullOrBlank()
private fun Virksomhet.erRegistrertIUtlLandetGyldigSatt(): Boolean = registrertIUtlandet !== null
private fun Virksomhet.erVirksomhetIUtlandet(): Boolean = !registrertINorge.boolean
private fun Virksomhet.erVirksomhetINorge() = registrertINorge == JaNei.Ja && registrertIUtlandet == null

