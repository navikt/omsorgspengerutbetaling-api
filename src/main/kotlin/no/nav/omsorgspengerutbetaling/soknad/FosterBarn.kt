package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonAlias
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

private val logger: Logger = LoggerFactory.getLogger("no.nav.omsorgspengerutbetaling.soknad.barn")

data class FosterBarn(
    @JsonAlias("identitetsnummer", "fødselsnummer") val fødselsnummer: String
)

data class Barn(
    val navn: String,
    val fødselsdato: LocalDate,
    val type: TypeBarn,
    val aktørId: String? = null,
    val utvidetRett: Boolean? = null,
    var identitetsnummer: String? = null
) {
    fun manglerIdentitetsnummer(): Boolean = identitetsnummer.isNullOrEmpty()

    infix fun oppdaterIdentitetsnummerMed(identitetsnummer: String?){
        logger.info("Oppdaterer identitetsnummer på barn")
        this.identitetsnummer = identitetsnummer
    }
}

enum class TypeBarn{
    FOSTERBARN,
    BARNET_BOR_I_UTLANDET,
    ANNET,
    FRA_OPPSLAG
}