package no.nav.omsorgspengerutbetaling.arbeidstakerutbetaling

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

data class JobbHosNåværendeArbeidsgiver(
    val merEnn4Uker: Boolean,
    val begrunnelse: Begrunnelse? = null
) {
    enum class Begrunnelse {
        ANNET_ARBEIDSFORHOLD,
        ANDRE_YTELSER,
        LOVBESTEMT_FERIE_ELLER_ULØNNET_PERMISJON,
        MILITÆRTJENESTE
    }
}

fun JobbHosNåværendeArbeidsgiver.valider() = mutableSetOf<Violation>().apply {
    if (merEnn4Uker == false && begrunnelse == null) {
        add(
            Violation(
                parameterType = ParameterType.ENTITY,
                parameterName = "jobbHosNåværendeArbeidsgiver.begrunnelse",
                reason = "Begrunnelse kan ikke være null, dersom merEnn4Uker er satt til false.",
                invalidValue = begrunnelse
            )
        )
    }
}
