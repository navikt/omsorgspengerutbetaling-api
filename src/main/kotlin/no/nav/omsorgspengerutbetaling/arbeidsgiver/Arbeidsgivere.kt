package no.nav.helse.arbeidsgiver

import no.nav.omsorgspengerutbetaling.felles.OrganisasjonDetaljer

data class ArbeidsgivereOppslagRespons (
    val arbeidsgivere: Arbeidsgivere
)

data class Arbeidsgivere (
    val organisasjoner: List<Organisasjon>
)

class Organisasjon (
    val organisasjonsnummer: String,
    val navn: String?
)

data class ArbeidsgiverDetaljer(
    val organisasjoner: List<OrganisasjonDetaljer>
)
