package no.nav.omsorgspengerutbetaling

import no.finn.unleash.Unleash

enum class FeatureFlag(val navn: String) {
    OMP_UT_SNF_SOKNAD_VALIDERING("sif.omp.ut.snf.api.soknad.validering")
}

fun Unleash.erAktiv(flagg: FeatureFlag, default: Boolean): Boolean = this.isEnabled(flagg.navn, default)
