package no.nav.omsorgspengerutbetaling.barn

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDate

data class BarnResponse(
    val barnOppslag: List<BarnOppslag>
)

data class BarnOppslag (
    val fødselsdato: LocalDate,
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val aktørId: String?,
    @JsonIgnore var identitetsnummer: String? = null
)

data class BarnOppslagResponse(
    val barn: List<BarnOppslagDTO>
)

data class BarnOppslagDTO(
    val fødselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
    val aktør_id: String,
    val identitetsnummer: String? = null
) {
    fun tilBarnOppslag() = BarnOppslag(
        fødselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn,
        aktørId = aktør_id,
        identitetsnummer = identitetsnummer
    )
}

