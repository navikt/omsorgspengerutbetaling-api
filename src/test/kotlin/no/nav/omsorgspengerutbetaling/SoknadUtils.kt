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
        spørsmål = listOf(
            SpørsmålOgSvar(id = SpørsmålId.HarBekreftetOpplysninger, spørsmål = "HarBekreftetOpplysninger?", svar = Svar.Ja),
            SpørsmålOgSvar(id = SpørsmålId.HarForståttRettigheterOgPlikter, spørsmål = "HarForståttRettigheterOgPlikter?", svar = Svar.Ja)
        ),
        utbetalingsperioder = listOf(
            UtbetalingsperiodeMedVedlegg(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(5)
            )
        )
    )
}

internal fun Søknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)