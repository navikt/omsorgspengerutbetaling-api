package no.nav.omsorgspengerutbetaling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.omsorgspengerutbetaling.soknad.*
import java.time.LocalDate

internal object SøknadUtils {
    internal val objectMapper = jacksonObjectMapper().omsorgspengerKonfiguert()

    internal val defaultSøknad = Søknad(
        språk = Språk.BOKMÅL,
        bosteder = listOf(),
        opphold = listOf(),
        jaNei = listOf(
            JaNei(id = JaNeiId.HarBekreftetOpplysninger, spørsmål = "HarBekreftetOpplysninger?", svar = JaNeiSvar.Ja),
            JaNei(id = JaNeiId.HarForståttRettigheterOgPlikter, spørsmål = "HarForståttRettigheterOgPlikter?", svar = JaNeiSvar.Ja)
        ),
        utbetalingsperioder = listOf(
            Utbetalingsperiode(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(5)
            )
        )
    )
}

internal fun Søknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)