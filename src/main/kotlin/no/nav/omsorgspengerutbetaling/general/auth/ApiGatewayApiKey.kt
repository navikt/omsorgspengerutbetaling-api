package no.nav.omsorgspengerutbetaling.general.auth

data class ApiGatewayApiKey(val value : String, val headerKey : String = "x-nav-apiKey")