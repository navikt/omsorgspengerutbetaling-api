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

    internal val defaultSøknad = Søknad(
        språk = Språk.BOKMÅL,
        bosteder = listOf(
            Bosted(
                fraOgMed = LocalDate.now().minusDays(20),
                tilOgMed = LocalDate.now().minusDays(10),
                landkode = "GB",
                landnavn = "Great Britain"
            )
        ),
        opphold = listOf(
            Opphold(
                fraOgMed = LocalDate.now().minusDays(20),
                tilOgMed = LocalDate.now().minusDays(10),
                landkode = "GB",
                landnavn = "Great Britain"
            )
        ),
        spørsmål = listOf(
            SpørsmålOgSvar(id = SpørsmålId.HarBekreftetOpplysninger, spørsmål = "HarBekreftetOpplysninger?", svar = Svar.Ja),
            SpørsmålOgSvar(id = SpørsmålId.HarForståttRettigheterOgPlikter, spørsmål = "HarForståttRettigheterOgPlikter?", svar = Svar.Ja)
        ),
        utbetalingsperioder = listOf(
            UtbetalingsperiodeMedVedlegg(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(5)
            )
        ),
        harHattInntektSomFrilanser = true,
        frilans = Frilans(
            startdato = LocalDate.now(),
            jobberFortsattSomFrilans = true
        ),
        harHattInntektSomSelvstendigNaringsdrivende = true,
        selvstendigVirksomheter = listOf(
            Virksomhet(
                naringstype = listOf(Naringstype.JORDBRUK),
                fraOgMed = LocalDate.now().minusDays(1),
                tilOgMed = LocalDate.now(),
                erPagaende = false,
                naringsinntekt = 123123,
                navnPaVirksomheten = "TullOgTøys",
                registrertINorge = true,
                organisasjonsnummer = "101010",
                yrkesaktivSisteTreFerdigliknedeArene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now()),
                harVarigEndringAvInntektSiste4Kalenderar = false,
                harRegnskapsforer = true,
                regnskapsforer = Regnskapsforer(
                    navn = "Kjell",
                    telefon = "84554",
                    erNarVennFamilie = false
                ),
                harRevisor = false
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
                fraOgMed = LocalDate.now().minusDays(20),
                tilOgMed = LocalDate.now().minusDays(10),
                landkode = "GB",
                landnavn = "Great Britain"
            )
        ),
        opphold = listOf(
            Opphold(
                fraOgMed = LocalDate.now().minusDays(20),
                tilOgMed = LocalDate.now().minusDays(10),
                landkode = "GB",
                landnavn = "Great Britain"
            )
        ),
        spørsmål = listOf(
            SpørsmålOgSvar(id = SpørsmålId.HarBekreftetOpplysninger, spørsmål = "HarBekreftetOpplysninger?", svar = Svar.Ja),
            SpørsmålOgSvar(id = SpørsmålId.HarForståttRettigheterOgPlikter, spørsmål = "HarForståttRettigheterOgPlikter?", svar = Svar.Ja)
        ),
        utbetalingsperioder = listOf(
            UtbetalingsperiodeUtenVedlegg(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(5),
                lengde = Duration.ofHours(7).plusMinutes(30)
            )
        ),
        frilans = Frilans(
            startdato = LocalDate.now(),
            jobberFortsattSomFrilans = true
        ),
        selvstendigVirksomheter = listOf(
            Virksomhet(
                naringstype = listOf(Naringstype.JORDBRUK),
                fraOgMed = LocalDate.now().minusDays(1),
                tilOgMed = LocalDate.now(),
                erPagaende = false,
                naringsinntekt = 123123,
                navnPaVirksomheten = "TullOgTøys",
                registrertINorge = true,
                organisasjonsnummer = "101010",
                yrkesaktivSisteTreFerdigliknedeArene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now()),
                harVarigEndringAvInntektSiste4Kalenderar = false,
                harRegnskapsforer = true,
                regnskapsforer = Regnskapsforer(
                    navn = "Kjell",
                    telefon = "84554",
                    erNarVennFamilie = false
                ),
                harRevisor = false
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

    fun bodyMedSelvstendigVirksomheterSomListe(virksomheter: List<Virksomhet>): Søknad {
        return Søknad(
            språk = Språk.BOKMÅL,
            bosteder = listOf(),
            opphold = listOf(),
            spørsmål = listOf(
                SpørsmålOgSvar(
                    id = SpørsmålId.HarBekreftetOpplysninger,
                    spørsmål = "HarBekreftetOpplysninger?",
                    svar = Svar.Ja
                ),
                SpørsmålOgSvar(
                    id = SpørsmålId.HarForståttRettigheterOgPlikter,
                    spørsmål = "HarForståttRettigheterOgPlikter?",
                    svar = Svar.Ja
                )
            ),
            utbetalingsperioder = listOf(
                UtbetalingsperiodeMedVedlegg(
                    fraOgMed = LocalDate.now(),
                    tilOgMed = LocalDate.now().plusDays(5)
                )
            ),
            harHattInntektSomFrilanser = true,
            frilans = Frilans(
                startdato = LocalDate.now(),
                jobberFortsattSomFrilans = true
            ),
            harHattInntektSomSelvstendigNaringsdrivende = true,
            selvstendigVirksomheter = virksomheter
        )
    }
}

internal fun Søknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)
internal fun KomplettSoknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)
