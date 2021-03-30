package no.nav.omsorgspengerutbetaling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.omsorgspengerutbetaling.k9format.tilKOmsorgspengerUtbetalingSøknad
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soknad.*
import no.nav.omsorgspengerutbetaling.soknad.Næringstyper.*
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

internal object SøknadUtils {
    internal val objectMapper = jacksonObjectMapper().omsorgspengerKonfiguert()
    val mottatt = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))

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
        utbetalingsperioder = listOf(
            Utbetalingsperiode(
                fraOgMed = start,
                tilOgMed = start.plusDays(10),
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = Duration.ofHours(3),
                årsak = FraværÅrsak.STENGT_SKOLE_ELLER_BARNEHAGE,
                aktivitetFravær = listOf(AktivitetFravær.FRILANSER)
            ),
            Utbetalingsperiode(
                fraOgMed = start.plusDays(20),
                tilOgMed = start.plusDays(20),
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = Duration.ofHours(3),
                årsak = FraværÅrsak.SMITTEVERNHENSYN,
                aktivitetFravær = listOf(AktivitetFravær.SELVSTENDIG_VIRKSOMHET)
            ),
            Utbetalingsperiode(
                fraOgMed = start.plusDays(30),
                tilOgMed = start.plusMonths(1).plusDays(4),
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = Duration.ofHours(3),
                årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                aktivitetFravær = listOf(AktivitetFravær.FRILANSER, AktivitetFravær.SELVSTENDIG_VIRKSOMHET)
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
        andreUtbetalinger = listOf(DAGPENGER, SYKEPENGER, MIDLERTIDIG_KOMPENSASJON_SN_FRI),
        frilans = Frilans(
            startdato = start,
            sluttdato = null,
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
                fiskerErPåBladB = JaNei.Nei,
                erNyoppstartet = true
            )
        ),
        erArbeidstakerOgså = true,
        fosterbarn = listOf(
            FosterBarn(
                fødselsnummer = "02119970078"
            )
        )
    )

    val søker = Søker(
        aktørId = "123456",
        fødselsnummer = "02119970078",
        fødselsdato = LocalDate.parse("1999-11-02"),
        etternavn = "Nordmann",
        mellomnavn = null,
        fornavn = "Ola",
        myndig = true
    )

    fun k9FormatSøknad(søknadId: SøknadId) = defaultSøknad.copy(søknadId = søknadId).tilKOmsorgspengerUtbetalingSøknad(
        mottatt = mottatt,
        søker = søker
    )

    internal fun defaultKomplettSøknad(søknadId: SøknadId) = KomplettSoknad(
        søknadId = søknadId,
        språk = Språk.BOKMÅL,
        mottatt = mottatt,
        søker = søker,
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
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = Duration.ofHours(3),
                årsak = FraværÅrsak.STENGT_SKOLE_ELLER_BARNEHAGE,
                aktivitetFravær = listOf(AktivitetFravær.FRILANSER)
            ),
            Utbetalingsperiode(
                fraOgMed = start.plusDays(20),
                tilOgMed = start.plusDays(20),
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = Duration.ofHours(3),
                årsak = FraværÅrsak.SMITTEVERNHENSYN,
                aktivitetFravær = listOf(AktivitetFravær.SELVSTENDIG_VIRKSOMHET)
            ),
            Utbetalingsperiode(
                fraOgMed = start.plusDays(30),
                tilOgMed = start.plusMonths(1).plusDays(4),
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = Duration.ofHours(3),
                årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                aktivitetFravær = listOf(AktivitetFravær.FRILANSER, AktivitetFravær.SELVSTENDIG_VIRKSOMHET)
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
                fiskerErPåBladB = JaNei.Nei,
                erNyoppstartet = true
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
        k9FormatSøknad = k9FormatSøknad(søknadId)
    )
}

internal fun Søknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)
internal fun KomplettSoknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)
