package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

data class Barn(
    var identitetsnummer: String? = null,
    val aktørId: String?,
    val navn: String,
    val aleneOmOmsorgen: Boolean? = null,
) {
    fun manglerIdentitetsnummer(): Boolean = identitetsnummer.isNullOrEmpty()

    infix fun oppdaterIdentitetsnummerMed(identitetsnummer: String?){
        this.identitetsnummer = identitetsnummer
    }

    fun valider(index: Int): MutableSet<Violation> {
        val mangler: MutableSet<Violation> = mutableSetOf()

        if(identitetsnummer.isNullOrBlank()){
            mangler.add(
                Violation(
                    parameterName = "barn[$index].identitetsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "Barn.identitetsnummer kan ikke være null eller blank",
                    invalidValue = identitetsnummer
                )
            )
        }

        if(identitetsnummer != null && !identitetsnummer!!.erGyldigNorskIdentifikator()){
            mangler.add(
                Violation(
                    parameterName = "barn[$index].identitetsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "Barn.identitetsnummer må være gyldig norsk identifikator",
                    invalidValue = identitetsnummer
                )
            )
        }

        if(navn.isBlank()){
            mangler.add(
                Violation(
                    parameterName = "barn[$index].navn",
                    parameterType = ParameterType.ENTITY,
                    reason = "Barn.navn kan ikke være tom eller bare inneholde mellomrom",
                    invalidValue = navn
                )
            )
        }

        if(aleneOmOmsorgen == null){
            mangler.add(
                Violation(
                    parameterName = "barn[$index].aleneOmOmsorgen",
                    parameterType = ParameterType.ENTITY,
                    reason = "Barn.aleneOmOmsorgen kan ikke være null",
                    invalidValue = aleneOmOmsorgen
                )
            )
        }

        return mangler
    }
}

fun List<Barn>.valider(): MutableSet<Violation> {
    val mangler: MutableSet<Violation> = mutableSetOf()

    forEachIndexed { index, barn -> mangler.addAll(barn.valider(index)) }

    return mangler
}
