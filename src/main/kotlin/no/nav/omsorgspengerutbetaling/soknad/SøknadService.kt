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

internal class SøknadService(
    private val omsorgpengesøknadMottakGateway: OmsorgpengesøknadMottakGateway,
    private val søkerService: SøkerService,
    private val vedleggService: VedleggService
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SøknadService::class.java)
    }

    internal suspend fun registrer(
        søknad: Søknad,
        idToken: IdToken,
        callId: CallId) {
        logger.trace("Registrerer søknad. Henter søker")

        val søker: Søker = søkerService.getSoker(idToken = idToken, callId = callId)

        logger.trace("Søker hentet. Validerer søker.")

        søker.validate()

        logger.trace("Søker Validert.")

        logger.trace("Henter legeerklæringer for ${søknad.utbetalingsperioder.size} utbetalingsperioder.")

        val utbetalingsperioder = søknad.utbetalingsperioder.map {
            logger.trace("Henter ${it.legeærklæringer} legeerklæringer.")
            UtbetalingsperiodeVedlegg(
                fraOgMed = it.fraOgMed,
                tilOgMed = it.tilOgMed,
                lengde = it.lengde,
                legeærklæringer = vedleggService.hentVedlegg(
                    idToken = idToken,
                    vedleggUrls = it.legeærklæringer,
                    callId = callId
                )
            )
        }

        logger.trace("Legeærkleringer hentet. Validerer dem.")

        val alleVedleggReferanser = søknad.utbetalingsperioder
            .map { it.legeærklæringer }
            .flatten()

        utbetalingsperioder.valider(alleVedleggReferanser = alleVedleggReferanser)

        logger.info("Legger søknad til prosessering")

        val komplettSoknad = KomplettSoknad(
            språk = søknad.språk,
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            søker = søker,
            bosteder = søknad.bosteder,
            opphold = søknad.opphold,
            jaNei = søknad.jaNei,
            utbetalingsperioder = utbetalingsperioder

        )

        omsorgpengesøknadMottakGateway.leggTilProsessering(
            soknad = komplettSoknad,
            callId = callId
        )

        logger.trace("Søknad lagt til prosessering. Sletter vedlegg.")


        vedleggService.slettVedleg(
            vedleggUrls = alleVedleggReferanser,
            callId = callId,
            idToken = idToken
        )

        logger.trace("Vedlegg slettet.")
    }
}

