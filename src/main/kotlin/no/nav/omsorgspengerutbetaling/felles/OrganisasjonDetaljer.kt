package no.nav.omsorgspengerutbetaling.felles

data class OrganisasjonDetaljer(
    val navn: String? = null,
    val skalJobbe: String,
    val organisasjonsnummer: String,
    val jobberNormaltTimer: Double,
    val skalJobbeProsent: Double,
    val vetIkkeEkstrainfo: String? = null
)
