package no.nav.omsorgspengerutbetaling.kafka

data class Metadata(
    val version : Int,
    val correlationId : String
)