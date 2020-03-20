package no.nav.omsorgspengerutbetaling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soknad.*
import no.nav.omsorgspengerutbetaling.vedlegg.Vedlegg
import java.nio.charset.Charset
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
                landnavn = "Great Britain"
            )
        ),
        opphold = listOf(
            Opphold(
                fraOgMed = start.minusDays(20),
                tilOgMed = start.minusDays(10),
                landkode = "GB",
                landnavn = "Great Britain"
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
        frilans = Frilans(
            startdato = start,
            jobberFortsattSomFrilans = JaNei.Ja
        ),
        selvstendigVirksomheter = listOf(
            Virksomhet(
                næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                fraOgMed = start.minusDays(1),
                tilOgMed = start,
                næringsinntekt = 123123,
                navnPaVirksomheten = "TullOgTøys",
                registrertINorge = JaNei.Ja,
                organisasjonsnummer = "101010",
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(start),
                regnskapsforer = Regnskapsforer(
                    navn = "Kjell",
                    telefon = "84554",
                    erNærVennFamilie = JaNei.Nei
                ),
                fiskerErPåBladB = JaNei.Nei
            )
        )
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
            fornavn = "Ola"
        ),
        bosteder = listOf(
            Bosted(
                fraOgMed = start.minusDays(20),
                tilOgMed = start.minusDays(10),
                landkode = "GB",
                landnavn = "Great Britain"
            )
        ),
        opphold = listOf(
            Opphold(
                fraOgMed = start.minusDays(20),
                tilOgMed = start.minusDays(10),
                landkode = "GB",
                landnavn = "Great Britain"
            )
        ),
        spørsmål = listOf(),
        utbetalingsperioder = listOf(
            UtbetalingsperiodeUtenVedlegg(
                fraOgMed = start,
                tilOgMed = start.plusDays(5),
                lengde = Duration.ofHours(7).plusMinutes(30)
            )
        ),
        frilans = Frilans(
            startdato = start,
            jobberFortsattSomFrilans = JaNei.Ja
        ),
        selvstendigVirksomheter = listOf(
            Virksomhet(
                næringstyper = listOf(Næringstyper.JORDBRUK_SKOGBRUK),
                fraOgMed = start.minusDays(1),
                tilOgMed = start,
                næringsinntekt = 123123,
                navnPaVirksomheten = "TullOgTøys",
                registrertINorge = JaNei.Ja,
                organisasjonsnummer = "101010",
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(start),
                regnskapsforer = Regnskapsforer(
                    navn = "Kjell",
                    telefon = "84554",
                    erNærVennFamilie = JaNei.Nei
                ),
                fiskerErPåBladB = JaNei.Nei
            )
        ),
        vedlegg = listOf(
            Vedlegg(
                content = "dette er et bilde :p".toByteArray(Charset.defaultCharset()),
                contentType = "img/pdf",
                title = "Navn på fil"
            )
        )
    )
}

internal fun Søknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)
internal fun KomplettSoknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)
