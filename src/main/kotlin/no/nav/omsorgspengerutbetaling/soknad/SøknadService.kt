package no.nav.omsorgspengerutbetaling.soknad

import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.auth.IdToken
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soker.SøkerService
import no.nav.omsorgspengerutbetaling.soker.validate
import no.nav.omsorgspengerutbetaling.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime


class SøknadService(
    private val omsorgpengesøknadMottakGateway: OmsorgpengesøknadMottakGateway,
    private val søkerService: SøkerService,
    private val vedleggService: VedleggService
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SøknadService::class.java)
    }

    suspend fun registrer(
        søknad: Søknad,
        idToken: IdToken,
        callId: CallId
    ) {
        logger.info("Registrerer søknad. Henter søker")
        val søker: Søker = søkerService.getSoker(idToken = idToken, callId = callId)

        logger.info("Søker hentet. Validerer søker.")
        søker.validate()

        logger.info("Søker Validert.")
        logger.info("Henter ${søknad.legeerklæring.size} legeerklæringsvedlegg.")
        val legeerklæring = vedleggService.hentVedlegg(
            idToken = idToken,
            vedleggUrls = søknad.legeerklæring,
            callId = callId
        )

        søknad.samværsavtale?.let { logger.info("Henter ${søknad.samværsavtale.size} samværsavtalevedlegg.") }
        val samværsavtale = when {
            !søknad.samværsavtale.isNullOrEmpty() -> {
                val samværsavtalevedlegg = vedleggService.hentVedlegg(
                    idToken = idToken,
                    vedleggUrls = søknad.samværsavtale,
                    callId = callId
                )
                logger.info("Hentet ${samværsavtalevedlegg.size} samværsavtalevedlegg.")
                samværsavtalevedlegg
            }
            else -> listOf()
        }

        logger.info("Vedlegg hentet. Validerer vedleggene.")
        legeerklæring.validerLegeerklæring(søknad.legeerklæring)
        søknad.samværsavtale?.let { samværsavtale.validerSamværsavtale(it) }
        val alleVedlegg = listOf(*legeerklæring.toTypedArray(), *samværsavtale.toTypedArray())
        alleVedlegg.validerTotalStørresle()

        logger.info("Legger søknad til prosessering")

        val komplettSoknad = KomplettSoknad(
            språk = søknad.språk,
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            søker = søker,
            barn = BarnDetaljer(
                fødselsdato = søknad.barn.fødselsdato,
                aktørId = søknad.barn.aktørId,
                navn = søknad.barn.navn,
                norskIdentifikator = søknad.barn.norskIdentifikator
            ),
            legeerklæring = legeerklæring,
            samværsavtale = samværsavtale,
            medlemskap = søknad.medlemskap,
            relasjonTilBarnet = søknad.relasjonTilBarnet,
            harBekreftetOpplysninger = søknad.harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = søknad.harForståttRettigheterOgPlikter,
            arbeidssituasjon = søknad.arbeidssituasjon,
            kroniskEllerFunksjonshemming = søknad.kroniskEllerFunksjonshemming,
            nyVersjon = søknad.nyVersjon,
            sammeAdresse = søknad.sammeAdresse
        )

        omsorgpengesøknadMottakGateway.leggTilProsessering(
            soknad = komplettSoknad,
            callId = callId
        )

        logger.trace("Søknad lagt til prosessering. Sletter vedlegg.")

        søknad.samværsavtale?.let {
            vedleggService.slettVedleg(
                vedleggUrls = it,
                callId = callId,
                idToken = idToken
            )
        }

        vedleggService.slettVedleg(
            vedleggUrls = søknad.legeerklæring,
            callId = callId,
            idToken = idToken
        )

        logger.trace("Vedlegg slettet.")
    }
}

