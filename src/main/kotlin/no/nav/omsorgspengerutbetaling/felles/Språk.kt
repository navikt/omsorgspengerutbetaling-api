package no.nav.omsorgspengerutbetaling.felles

import com.fasterxml.jackson.annotation.JsonValue

enum class Språk(@JsonValue val språk: String) {
    BOKMÅL("nb"),
    NYNORSK("nn");
}
