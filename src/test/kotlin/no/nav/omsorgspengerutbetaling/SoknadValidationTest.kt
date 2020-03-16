package no.nav.omsorgspengerutbetaling

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.omsorgspengerutbetaling.soknad.*
import org.junit.Test
import java.net.URL
import java.time.LocalDate
import kotlin.test.assertTrue


internal class SøknadValideringsTest {

    companion object {
        private val gyldigFodselsnummerA = "02119970078"
        private val gyldigFodselsnummerB = "19066672169"
        private val gyldigFodselsnummerC = "20037473937"
        private val dNummerA = "55125314561"
    }

    @Test(expected = Throwblem::class)
    internal fun `Til dato kan ikke være før fra dato`() {
        Søknad(
            nyVersjon = false,
            språk = "nb",
            arbeidssituasjon = listOf("Arbeidstaker", "Frilans", "Selvstendig Næringsdrivende"),
            kroniskEllerFunksjonshemming = true,
            sammeAdresse = true,
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true,
            relasjonTilBarnet = SøkerBarnRelasjon.FAR,
            barn = BarnDetaljer(
                navn = "Ole Dole Doffen",
                fødselsdato = LocalDate.now().minusDays(895),
                aktørId = "123456"
            ),
            medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = false,
                skalBoIUtlandetNeste12Mnd = true,
                utenlandsoppholdNeste12Mnd = listOf(
                    Utenlandsopphold(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().minusDays(1),
                        landkode = "NO",
                        landnavn = "Norge"
                    )
                )
            ),
            samværsavtale = listOf(
                URL("http://localhost:8080/vedlegg/1"),
                URL("http://localhost:8080/vedlegg/2")
            ),
            legeerklæring = listOf(
                URL("http://localhost:8080/vedlegg/3"),
                URL("http://localhost:8080/vedlegg/4")
            )
        ).valider()
    }

    @Test(expected = Throwblem::class)
    internal fun `Mangler landkode`() {
        val søknad = Søknad(
            nyVersjon = false,
            språk = "nb",
            arbeidssituasjon = listOf("Arbeidstaker", "Frilans", "Selvstendig Næringsdrivende"),
            kroniskEllerFunksjonshemming = true,
            sammeAdresse = true,
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true,
            relasjonTilBarnet = SøkerBarnRelasjon.FAR,
            barn = BarnDetaljer(
                navn = "Ole Dole Doffen",
                fødselsdato = LocalDate.now().minusDays(895),
                aktørId = "123456"
            ),
            medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = false,
                skalBoIUtlandetNeste12Mnd = true,
                utenlandsoppholdNeste12Mnd = listOf(
                    Utenlandsopphold(
                        fraOgMed = LocalDate.now().minusDays(5),
                        tilOgMed = LocalDate.now(),
                        landkode = "",
                        landnavn = "Norge"
                    )
                )
            ),
            samværsavtale = null,
            legeerklæring = listOf(
                URL("http://localhost:8080/vedlegg/1")
            )
        ).valider()
    }

    @Test
    fun `Tester gyldig fødselsdato dersom dnunmer`() {
        val starterMedFodselsdato = "630293".starterMedFodselsdato()
        assertTrue(starterMedFodselsdato)
    }

    @Test(expected = Throwblem::class)
    fun `Forvent violation dersom barn har både fødselsdato og norskIdentnummer`() {
        Søknad(
            nyVersjon = false,
            språk = "nb",
            arbeidssituasjon = listOf("Arbeidstaker", "Frilans", "Selvstendig Næringsdrivende"),
            kroniskEllerFunksjonshemming = true,
            sammeAdresse = true,
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true,
            relasjonTilBarnet = SøkerBarnRelasjon.FAR,
            barn = BarnDetaljer(
                navn = "Ole Dole Doffen",
                fødselsdato = LocalDate.now().minusDays(895),
                norskIdentifikator = gyldigFodselsnummerA
            ),
            medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = false,
                skalBoIUtlandetNeste12Mnd = false
            ),
            samværsavtale = null,
            legeerklæring = listOf(
                URL("http://localhodt:8080/vedlegg/1")
            )
        ).valider()
    }

    @Test(expected = Throwblem::class)
    fun `Forvent violation dersom barn mangler både fødselsdato og norskIdentnummer`() {
        Søknad(
            nyVersjon = false,
            språk = "nb",
            arbeidssituasjon = listOf("Arbeidstaker", "Frilans", "Selvstendig Næringsdrivende"),
            kroniskEllerFunksjonshemming = true,
            sammeAdresse = true,
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true,
            relasjonTilBarnet = SøkerBarnRelasjon.FAR,
            barn = BarnDetaljer(
                navn = "Ole Dole Doffen",
                fødselsdato = null,
                norskIdentifikator = null
            ),
            medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = false,
                skalBoIUtlandetNeste12Mnd = false
            ),
            samværsavtale = null,
            legeerklæring = listOf(
                URL("http://localhodt:8080/vedlegg/1")
            )
        ).valider()
    }

    @Test(expected = Throwblem::class)
    internal fun `Forvent violation dersom harBoddIUtlandetSiste12Mnd er true, men utenlandsoppholdSiste12Mnd er tom eller null`() {
        Søknad(
            nyVersjon = false,
            språk = "nb",
            arbeidssituasjon = listOf("Arbeidstaker", "Frilans", "Selvstendig Næringsdrivende"),
            kroniskEllerFunksjonshemming = true,
            sammeAdresse = true,
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true,
            relasjonTilBarnet = SøkerBarnRelasjon.FAR,
            barn = BarnDetaljer(
                navn = "Ole Dole Doffen",
                fødselsdato = LocalDate.now().minusDays(895),
                aktørId = "123456"
            ),
            medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = true,
                skalBoIUtlandetNeste12Mnd = false,
                utenlandsoppholdSiste12Mnd = listOf()
            ),
            samværsavtale = listOf(
            ),
            legeerklæring = listOf(
                URL("http://localhost:8080/vedlegg/1")
            )
        ).valider()
    }
}
