package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonAlias

enum class AndreUtbetalinger{ // TODO: 24/01/2022 Fjerne JsonAlias når frontend kun sender enum
    @JsonAlias ("dagpenger") DAGPENGER,
    @JsonAlias ("sykepenger") SYKEPENGER,
    @JsonAlias ("midlertidigkompensasjonsnfri") MIDLERTIDIG_KOMPENSASJON_SN_FRI
}