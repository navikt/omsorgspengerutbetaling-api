package no.nav.omsorgspengerutbetaling.arbeidstakerutbetaling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.arbeidstakerutbetaling.JobbHosNåværendeArbeidsgiver.Begrunnelse.ANNET_ARBEIDSFORHOLD
import no.nav.omsorgspengerutbetaling.felles.*
import no.nav.omsorgspengerutbetaling.omsorgspengerKonfiguert
import no.nav.omsorgspengerutbetaling.soker.Søker
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

internal object ArbeidstakerutbetalingSøknadUtils {
    internal val objectMapper = jacksonObjectMapper().omsorgspengerKonfiguert()
    private val start = LocalDate.parse("2020-01-01")
    private const val GYLDIG_ORGNR = "917755736"

    internal val defaultSøknad = Arbeidstakerutbetalingsøknad(
        språk = Språk.BOKMÅL,
        jobbHosNåværendeArbeidsgiver = JobbHosNåværendeArbeidsgiver(
            merEnn4Uker = true,
            begrunnelse = ANNET_ARBEIDSFORHOLD
        ),
        arbeidsgivere = ArbeidsgiverDetaljer(
            organisasjoner = listOf(
                OrganisasjonDetaljer(
                    navn = "Arbeidsgiver 1",
                    organisasjonsnummer = GYLDIG_ORGNR,
                    harHattFraværHosArbeidsgiver = true,
                    arbeidsgiverHarUtbetaltLønn = false
                ),
                OrganisasjonDetaljer(
                    navn = "Arbeidsgiver 2",
                    organisasjonsnummer = GYLDIG_ORGNR,
                    harHattFraværHosArbeidsgiver = true,
                    arbeidsgiverHarUtbetaltLønn = false
                ),
                OrganisasjonDetaljer(
                    navn = "Arbeidsgiver 3",
                    organisasjonsnummer = GYLDIG_ORGNR,
                    harHattFraværHosArbeidsgiver = true,
                    arbeidsgiverHarUtbetaltLønn = false
                ),
                OrganisasjonDetaljer(
                    organisasjonsnummer = GYLDIG_ORGNR,
                    harHattFraværHosArbeidsgiver = true,
                    arbeidsgiverHarUtbetaltLønn = false
                )
            )
        ),
        bosteder = listOf(
            Bosted(
                fraOgMed = start.minusDays(20),
                tilOgMed = start.minusDays(10),
                landkode = "GB",
                landnavn = "Great Britain",
                erEØSLand = JaNei.Ja
            )
        ),
        opphold = listOf(
            Opphold(
                fraOgMed = start.minusDays(20),
                tilOgMed = start.minusDays(10),
                landkode = "GB",
                landnavn = "Great Britain",
                erEØSLand = JaNei.Ja
            )
        ),
        spørsmål = listOf(
            SpørsmålOgSvar(
                spørsmål = "Et spørsmål",
                svar = JaNei.Nei
            )
        ),
        bekreftelser = Bekreftelser(
            harForståttRettigheterOgPlikter = JaNei.Ja,
            harBekreftetOpplysninger = JaNei.Ja
        ),
        utbetalingsperioder = listOf(
            Utbetalingsperiode(
                fraOgMed = start,
                tilOgMed = start.plusDays(5)
            )
        ),
        andreUtbetalinger = listOf(DAGPENGER, SYKEPENGER),
        fosterbarn = listOf(
            FosterBarn(
                fødselsnummer = "02119970078",
                fornavn = "fornavn",
                etternavn = "etternavn"
            )
        )
    )

    internal val defaultKomplettSøknad = KomplettArbeidstakerutbetalingsøknad(
        språk = Språk.BOKMÅL,
        mottatt = ZonedDateTime.now(),
        søker = Søker(
            aktørId = "123456",
            fødselsnummer = "02119970078",
            fødselsdato = LocalDate.parse("1999-11-02"),
            etternavn = "Nordmann",
            mellomnavn = null,
            fornavn = "Ola",
            myndig = true
        ),
        jobbHosNåværendeArbeidsgiver = JobbHosNåværendeArbeidsgiver(
            merEnn4Uker = true,
            begrunnelse = ANNET_ARBEIDSFORHOLD
        ),
        arbeidsgivere = ArbeidsgiverDetaljer(
            organisasjoner = listOf(
                OrganisasjonDetaljer(
                    navn = "Arbeidsgiver 1",
                    organisasjonsnummer = GYLDIG_ORGNR,
                    harHattFraværHosArbeidsgiver = true,
                    arbeidsgiverHarUtbetaltLønn = false
                ),
                OrganisasjonDetaljer(
                    navn = "Arbeidsgiver 2",
                    organisasjonsnummer = GYLDIG_ORGNR,
                    harHattFraværHosArbeidsgiver = true,
                    arbeidsgiverHarUtbetaltLønn = false
                ),
                OrganisasjonDetaljer(
                    navn = "Arbeidsgiver 3",
                    organisasjonsnummer = GYLDIG_ORGNR,
                    harHattFraværHosArbeidsgiver = true,
                    arbeidsgiverHarUtbetaltLønn = false
                ),
                OrganisasjonDetaljer(
                    organisasjonsnummer = GYLDIG_ORGNR,
                    harHattFraværHosArbeidsgiver = true,
                    arbeidsgiverHarUtbetaltLønn = false
                )
            )
        ),
        bosteder = listOf(
            Bosted(
                fraOgMed = start.minusDays(20),
                tilOgMed = start.minusDays(10),
                landkode = "GB",
                landnavn = "Great Britain",
                erEØSLand = JaNei.Ja
            )
        ),
        opphold = listOf(
            Opphold(
                fraOgMed = start.minusDays(20),
                tilOgMed = start.minusDays(10),
                landkode = "GB",
                landnavn = "Great Britain",
                erEØSLand = JaNei.Ja
            )
        ),
        spørsmål = listOf(
            SpørsmålOgSvar(
                spørsmål = "Et spørsmål",
                svar = JaNei.Nei
            )
        ),
        utbetalingsperioder = listOf(
            Utbetalingsperiode(
                fraOgMed = start,
                tilOgMed = start.plusDays(10),
                lengde = null
            ),
            Utbetalingsperiode(
                fraOgMed = start.plusDays(20),
                tilOgMed = start.plusDays(20),
                lengde = Duration.ofHours(5).plusMinutes(30)
            ),
            Utbetalingsperiode(
                fraOgMed = start.plusDays(30),
                tilOgMed = start.plusMonths(1).plusDays(4),
                lengde = Duration.ofHours(7).plusMinutes(30)
            )
        ),
        andreUtbetalinger = listOf(DAGPENGER, SYKEPENGER),
        fosterbarn = listOf(
            FosterBarn(
                fødselsnummer = "02119970078",
                fornavn = "fornavn",
                etternavn = "etternavn"
            )
        ),
        bekreftelser = Bekreftelser(
            harForståttRettigheterOgPlikter = JaNei.Ja,
            harBekreftetOpplysninger = JaNei.Ja
        )
    )
}

internal fun Arbeidstakerutbetalingsøknad.somJson() = ArbeidstakerutbetalingSøknadUtils.objectMapper.writeValueAsString(this)
internal fun KomplettArbeidstakerutbetalingsøknad.somJson() = ArbeidstakerutbetalingSøknadUtils.objectMapper.writeValueAsString(this)
