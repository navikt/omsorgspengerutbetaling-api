package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.LocalDate

data class Virksomhet(
    val naringstype: List<Naringstype>,
    val fiskerErPåBladB: Boolean? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate? = null,
    val erPagaende: Boolean,
    val naringsinntekt: Int? = null,
    val navnPaVirksomheten: String,
    val organisasjonsnummer: String? = null,
    val registrertINorge: Boolean,
    val registrertILand: String? = null,
    val harBlittYrkesaktivSisteTreFerdigliknendeArene: Boolean? = null,
    val yrkesaktivSisteTreFerdigliknedeArene: YrkesaktivSisteTreFerdigliknedeArene? = null,
    val harVarigEndringAvInntektSiste4Kalenderar: Boolean? = null,
    val varigEndring: VarigEndring? = null,
    val harRegnskapsforer: Boolean,
    val regnskapsforer: Regnskapsforer? = null,
    val harRevisor: Boolean? = null,
    val revisor: Revisor? = null
)

data class YrkesaktivSisteTreFerdigliknedeArene(
    val oppstartsdato: LocalDate?
)

enum class Naringstype(val detaljert: String) {
    @JsonProperty("FISKE") FISKER("FISKE"),
    @JsonProperty("JORDBRUK_SKOGBRUK") JORDBRUK("JORDBRUK_SKOGBRUK"),
    @JsonProperty("ANNEN") ANNET("ANNEN"),
    DAGMAMMA("DAGMAMMA")
}

data class VarigEndring(
    val dato: LocalDate? = null,
    val inntektEtterEndring: Int? = null,
    val forklaring: String? = null
)

data class Revisor(
    val navn: String,
    val telefon: String,
    val erNarVennFamilie: Boolean,
    val kanInnhenteOpplysninger: Boolean?
)

data class Regnskapsforer(
    val navn: String,
    val telefon: String,
    val erNarVennFamilie: Boolean
)


internal fun Virksomhet.validate(): MutableSet<Violation>{
    val violations = mutableSetOf<Violation>()

    if(!harGyldigPeriode()){
        violations.add(
            Violation(
                parameterName = "virksomhet.tilogmed og virksomhet.fraogmed",
                parameterType = ParameterType.ENTITY,
                reason = "Har ikke gyldig periode. Fraogmed kan ikke være nyere enn tilogmed",
                invalidValue = tilOgMed
            )
        )
    }

    if(!erErPaagaendeGyldigSatt()){
        violations.add(
            Violation(
                parameterName = "erPagaende",
                parameterType = ParameterType.ENTITY,
                reason = "erPagaende er ikke gyldig satt. Hvis pågående så kan ikke tilogmed være satt",
                invalidValue = erPagaende
            )
        )
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

    if(!erRegnskapsførerSattGyldig()){
        violations.add(
            Violation(
                parameterName = "regnskapsforer",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis man har satt harRegnskapsforer til true så må regnskapsforer være satt til et regnskapsforer objekt",
                invalidValue = regnskapsforer
            )
        )
    }

    if(!erRevisorSattGyldig()){
        violations.add(
            Violation(
                parameterName = "revisor",
                parameterType = ParameterType.ENTITY,
                reason = "Hvis man har satt harRevisor til true så må revisor være satt til et revisor objekt",
                invalidValue = revisor
            )
        )
    }

    if(!erFiskerSattGyldig()){
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

internal fun Virksomhet.harGyldigPeriode(): Boolean {
    val now = LocalDate.now();

    if (erPagaende) return now >= fraOgMed
    return fraOgMed <= tilOgMed
}

internal fun Virksomhet.erRevisorSattGyldig(): Boolean{
    if(harRevisor != null && harRevisor) return revisor != null
    return true
}

internal fun Virksomhet.erErPaagaendeGyldigSatt(): Boolean{
    if (erPagaende) return tilOgMed == null
    return tilOgMed != null
}

internal fun Virksomhet.erRegistrertINorgeGyldigSatt(): Boolean{
    if(registrertINorge) return !organisasjonsnummer.isNullOrEmpty()
    return true
}

internal fun Virksomhet.erRegistrertILandGyldigSatt(): Boolean{
    if(!registrertINorge) return registrertILand != null && registrertILand.isNotEmpty()
    return true
}

internal fun Virksomhet.erRegnskapsførerSattGyldig() : Boolean{
    if(harRegnskapsforer) return regnskapsforer != null
    return true
}

internal fun Virksomhet.erFiskerSattGyldig(): Boolean{
    if(naringstype.contains(Naringstype.FISKER)){
        return fiskerErPåBladB != null
    }
    return true
}
