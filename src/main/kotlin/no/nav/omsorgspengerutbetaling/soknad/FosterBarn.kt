package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonAlias

data class FosterBarn(
    @JsonAlias("identitetsnummer", "fødselsnummer") val fødselsnummer: String
)
