package no.nav.omsorgspengerutbetaling.general

import io.ktor.application.*
import io.ktor.features.*
import no.nav.omsorgspengerutbetaling.kafka.Metadata

data class CallId(val value : String)

fun ApplicationCall.getCallId() : CallId {
    return CallId(callId!!)
}

fun ApplicationCall.getMetadata() = Metadata(
    version = 1,
    correlationId = getCallId().value
)
