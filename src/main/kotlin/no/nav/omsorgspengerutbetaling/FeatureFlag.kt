package no.nav.omsorgspengerutbetaling

import no.nav.helse.dusseldorf.ktor.unleash.UnleashFeature

enum class FeatureFlag : UnleashFeature {
    OMP_UT_SNF_SOKNAD_VALIDERING() {
        override fun featureName(): String = "sif.omp.ut.snf.api.soknad.validering"

    }
}
