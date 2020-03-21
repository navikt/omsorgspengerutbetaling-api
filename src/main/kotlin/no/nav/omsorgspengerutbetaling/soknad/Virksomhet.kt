package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

data class Virksomhet(
    val næringstyper: List<Næringstyper> = listOf(),
    val fiskerErPåBladB: JaNei?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate? = null,
    val næringsinntekt: Int? = null,
    val navnPåVirksomheten: String,
    val organisasjonsnummer: String? = null,
    val registrertINorge: JaNei,
    val registrertILand: String? = null,
    val yrkesaktivSisteTreFerdigliknedeÅrene: YrkesaktivSisteTreFerdigliknedeArene? = null,
    val varigEndring: VarigEndring? = null,
    val regnskapsfører: Regnskapsfører? = null,
    val revisor: Revisor? = null
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

data class Revisor(
    val navn: String,
    val telefon: String,
    val kanInnhenteOpplysninger: JaNei
)

data class Regnskapsfører(
    val navn: String,
    val telefon: String
)


internal fun Virksomhet.validate(): MutableSet<Violation>{
    val violations = mutableSetOf<Violation>()

    tilOgMed?.apply {
        violations.addAll(Periode(fraOgMed, tilOgMed).valider())
    }

    if(!erRegistrertINorgeGyldigSatt()){
        violations.add(
            Violation(
                parameterName = "organisasjonsnummer",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis registrertINorge er true så må også organisasjonsnummer være satt",
                invalidValue = organisasjonsnummer
            )
        )
    }

    if(!erRegistrertILandGyldigSatt()){
        violations.add(
            Violation(
                parameterName = "registrertILand",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis registrertINorge er false så må registrertILand være satt til noe",
                invalidValue = registrertILand
            )
        )
    }

    if(!erFiskerGyldigSatt()){
        violations.add(
            Violation(
                parameterName = "fiskerErPåBladB",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis fisker er satt som naringstype, så må fiskerErPåBladB være satt til true eller false, ikke null",
                invalidValue = fiskerErPåBladB
            )
        )
    }
    return violations
}


private fun Virksomhet.erRegistrertINorgeGyldigSatt(): Boolean{
    if (registrertINorge == JaNei.Ja) return !organisasjonsnummer.isNullOrEmpty()
    return true
}

private fun Virksomhet.erRegistrertILandGyldigSatt() =
    registrertINorge == JaNei.Ja || registrertILand != null && registrertILand.isNotBlank()


private fun Virksomhet.erFiskerGyldigSatt(): Boolean{
    if (næringstyper.contains(Næringstyper.FISKE)){
        return fiskerErPåBladB != null
    }
    return true
}
