package no.nav.omsorgspengerutbetaling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soknad.*
import no.nav.omsorgspengerutbetaling.soknad.Næringstyper.*
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

internal object SøknadUtils {
    internal val objectMapper = jacksonObjectMapper().omsorgspengerKonfiguert()
    private val start = LocalDate.parse("2020-01-01")

    internal val defaultSøknad = Søknad(
        språk = Språk.BOKMÅL,
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
            UtbetalingsperiodeMedVedlegg(
                fraOgMed = start,
                tilOgMed = start.plusDays(5)
            )
        ),
        andreUtbetalinger = listOf(DAGPENGER, SYKEPENGER, MIDLERTIDIG_KOMPENSASJON_SN_FRI),
        frilans = Frilans(
            startdato = start,
            jobberFortsattSomFrilans = JaNei.Ja
        ),
        selvstendigVirksomheter = listOf(
            Virksomhet(
                næringstyper = listOf(JORDBRUK_SKOGBRUK, FISKE, DAGMAMMA, ANNEN),
                fraOgMed = start.minusDays(1),
                tilOgMed = start,
                næringsinntekt = 123123,
                navnPåVirksomheten = "TullOgTøys",
                registrertINorge = JaNei.Nei,
                registrertILand = "Tyskland",
                registrertIUtlandet = Land(
                    landkode = "DEU",
                    landnavn = "Tyskland"
                ),
                organisasjonsnummer = "101010",
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(start),
                regnskapsfører = Regnskapsfører(
                    navn = "Kjell",
                    telefon = "84554"
                ),
                fiskerErPåBladB = JaNei.Nei
            )
        ),
        erArbeidstakerOgså = true,
        fosterbarn = listOf(
            FosterBarn(
                fødselsnummer = "02119970078"
            )
        ),
        hjemmePgaSmittevernhensyn = true,
        hjemmePgaStengtBhgSkole = true
    )

    internal val defaultKomplettSøknad = KomplettSoknad(
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
            UtbetalingsperiodeUtenVedlegg(
                fraOgMed = start,
                tilOgMed = start.plusDays(10),
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = Duration.ofHours(3)
            ),
            UtbetalingsperiodeUtenVedlegg(
                fraOgMed = start.plusDays(20),
                tilOgMed = start.plusDays(20),
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = Duration.ofHours(3)
            ),
            UtbetalingsperiodeUtenVedlegg(
                fraOgMed = start.plusDays(30),
                tilOgMed = start.plusMonths(1).plusDays(4),
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = Duration.ofHours(3)
            )
        ),
        andreUtbetalinger = listOf(DAGPENGER, SYKEPENGER),
        frilans = Frilans(
            startdato = start,
            jobberFortsattSomFrilans = JaNei.Ja
        ),
        selvstendigVirksomheter = listOf(
            Virksomhet(
                næringstyper = listOf(JORDBRUK_SKOGBRUK, FISKE, DAGMAMMA, ANNEN),
                fraOgMed = start.minusDays(1),
                tilOgMed = start,
                næringsinntekt = 123123,
                navnPåVirksomheten = "TullOgTøys",
                registrertINorge = JaNei.Nei,
                registrertILand = "Tyskland",
                registrertIUtlandet = Land(
                    landkode = "DEU",
                    landnavn = "Tyskland"
                ),
                organisasjonsnummer = "101010",
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(start),
                regnskapsfører = Regnskapsfører(
                    navn = "Kjell",
                    telefon = "84554"
                ),
                varigEndring = VarigEndring(
                    dato = start,
                    inntektEtterEndring = 1337,
                    forklaring = "Fordi"
                ),
                fiskerErPåBladB = JaNei.Nei
            )
        ),
        erArbeidstakerOgså = true,
        fosterbarn = listOf(
            FosterBarn(
                fødselsnummer = "02119970078"
            )
        ),
        vedlegg = listOf(),
        bekreftelser = Bekreftelser(
            harForståttRettigheterOgPlikter = JaNei.Ja,
            harBekreftetOpplysninger = JaNei.Ja
        ),
        hjemmePgaSmittevernhensyn = true,
        hjemmePgaStengtBhgSkole = true
    )
}

internal fun Søknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)
internal fun KomplettSoknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)
