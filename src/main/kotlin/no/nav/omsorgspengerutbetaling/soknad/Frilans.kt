package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class  Frilans(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startdato: LocalDate,
    val jobberFortsattSomFrilans: JaNei
)
