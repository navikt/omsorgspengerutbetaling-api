package no.nav.omsorgspengerutbetaling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.omsorgspengerutbetaling.k9format.tilK9Format
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soknad.*
import no.nav.omsorgspengerutbetaling.soknad.AktivitetFravær.FRILANSER
import no.nav.omsorgspengerutbetaling.soknad.AktivitetFravær.SELVSTENDIG_VIRKSOMHET
import no.nav.omsorgspengerutbetaling.soknad.AndreUtbetalinger.*
import no.nav.omsorgspengerutbetaling.soknad.FraværÅrsak.*
import no.nav.omsorgspengerutbetaling.soknad.Næringstyper.*
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

internal object SøknadUtils {
    internal val objectMapper = jacksonObjectMapper().omsorgspengerKonfiguert()
    val mottatt = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))

    private val start = LocalDate.parse("2020-01-01")

    fun hentGyldigSøknad() = Søknad(
        mottatt = mottatt,
        språk = Språk.BOKMÅL,
        harDekketTiFørsteDagerSelv = true,
        bosteder = listOf(
            Bosted(
                fraOgMed = start.minusDays(20),
                tilOgMed = start.minusDays(10),
                landkode = "GBR",
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
                årsak = STENGT_SKOLE_ELLER_BARNEHAGE,
                aktivitetFravær = listOf(FRILANSER)
            ),
            Utbetalingsperiode(
                fraOgMed = start.plusDays(20),
                tilOgMed = start.plusDays(20),
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = Duration.ofHours(3),
                årsak = SMITTEVERNHENSYN,
                aktivitetFravær = listOf(SELVSTENDIG_VIRKSOMHET)
            ),
            Utbetalingsperiode(
                fraOgMed = start.plusDays(30),
                tilOgMed = start.plusMonths(1).plusDays(4),
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = Duration.ofHours(3),
                årsak = ORDINÆRT_FRAVÆR,
                aktivitetFravær = listOf(FRILANSER, SELVSTENDIG_VIRKSOMHET)
            )
        ),
        opphold = listOf(
            Opphold(
                fraOgMed = start.minusDays(20),
                tilOgMed = start.minusDays(10),
                landkode = "GBR",
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
        selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
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
            organisasjonsnummer = "916974574",
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
            erNyoppstartet = true,
            harFlereAktiveVirksomheter = true

        ),
        erArbeidstakerOgså = true,
        barn = listOf(
            Barn(
                navn = "Barn Barnesen",
                type = TypeBarn.FOSTERBARN,
                fødselsdato = LocalDate.parse("2021-01-01"),
                aktørId = "1000000000001",
                identitetsnummer = "16012099359"
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

    fun k9FormatSøknad(søknadId: SøknadId) = hentGyldigSøknad().copy(søknadId = søknadId).tilK9Format(søker = søker)

    internal fun defaultKomplettSøknad(søknadId: SøknadId) = KomplettSøknad(
        søknadId = søknadId,
        språk = Språk.BOKMÅL,
        mottatt = mottatt,
        søker = søker,
        harDekketTiFørsteDagerSelv = true,
        bosteder = listOf(
            Bosted(
                fraOgMed = start.minusDays(20),
                tilOgMed = start.minusDays(10),
                landkode = "GBR",
                landnavn = "Great Britain",
                erEØSLand = JaNei.Ja
            )
        ),
        opphold = listOf(
            Opphold(
                fraOgMed = start.minusDays(20),
                tilOgMed = start.minusDays(10),
                landkode = "GBR",
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
                årsak = STENGT_SKOLE_ELLER_BARNEHAGE,
                aktivitetFravær = listOf(FRILANSER)
            ),
            Utbetalingsperiode(
                fraOgMed = start.plusDays(20),
                tilOgMed = start.plusDays(20),
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = Duration.ofHours(3),
                årsak = SMITTEVERNHENSYN,
                aktivitetFravær = listOf(SELVSTENDIG_VIRKSOMHET)
            ),
            Utbetalingsperiode(
                fraOgMed = start.plusDays(30),
                tilOgMed = start.plusMonths(1).plusDays(4),
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = Duration.ofHours(3),
                årsak = ORDINÆRT_FRAVÆR,
                aktivitetFravær = listOf(FRILANSER, SELVSTENDIG_VIRKSOMHET)
            )
        ),
        andreUtbetalinger = listOf(DAGPENGER, SYKEPENGER),
        frilans = Frilans(
            startdato = start,
            jobberFortsattSomFrilans = JaNei.Ja
        ),
        selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            harFlereAktiveVirksomheter = true,
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
            organisasjonsnummer = "916974574",
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

        ),
        erArbeidstakerOgså = true,
        barn = listOf(
            Barn(
                navn = "Barn Barnesen",
                type = TypeBarn.ANNET,
                fødselsdato = LocalDate.parse("2021-01-01"),
                aktørId = "1000000000001",
                identitetsnummer = "16012099359"
            )
        ),
        vedleggId = listOf("1", "2","3"),
        bekreftelser = Bekreftelser(
            harForståttRettigheterOgPlikter = JaNei.Ja,
            harBekreftetOpplysninger = JaNei.Ja
        ),
        k9FormatSøknad = k9FormatSøknad(søknadId)
    )
}

internal fun Søknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)
internal fun KomplettSøknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)
