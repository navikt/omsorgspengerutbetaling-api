package no.nav.omsorgspengerutbetaling.arbeidstakerutbetaling

import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.auth.IdToken
import no.nav.omsorgspengerutbetaling.mottak.OmsorgpengesøknadMottakGateway
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soker.SøkerService
import no.nav.omsorgspengerutbetaling.soker.validate
import no.nav.omsorgspengerutbetaling.vedlegg.Vedlegg
import no.nav.omsorgspengerutbetaling.vedlegg.VedleggService

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime

internal class ArbeidstakerutbetalingSøknadService(
    private val omsorgpengesøknadMottakGateway: OmsorgpengesøknadMottakGateway,
    private val søkerService: SøkerService,
    private val vedleggService: VedleggService
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(ArbeidstakerutbetalingSøknadService::class.java)
    }

    internal suspend fun registrer(
        søknad: Arbeidstakerutbetalingsøknad,
        idToken: IdToken,
        callId: CallId) {
        logger.trace("Registrerer søknad. Henter søker")

        val søker: Søker = søkerService.getSoker(idToken = idToken, callId = callId)

        logger.trace("Søker hentet. Validerer søker.")

        søker.validate()
        logger.trace("Søker Validert.")

        logger.trace("Henter ${søknad.vedlegg.size} vedlegg.")
        val vedlegg: List<Vedlegg> = vedleggService.hentVedlegg(
            idToken = idToken,
            vedleggUrls = søknad.vedlegg,
            callId = callId
        )

        logger.trace("Vedlegg hentet. Validerer vedlegg.")
        vedlegg.validerVedlegg(søknad.vedlegg)
        logger.info("Vedlegg validert")

        logger.info("Legger søknad til prosessering")

        val komplettSoknad = KomplettArbeidstakerutbetalingsøknad(
            språk = søknad.språk,
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            søker = søker,
            bosteder = søknad.bosteder,
            opphold = søknad.opphold,
            spørsmål = søknad.spørsmål,
            jobbHosNåværendeArbeidsgiver = søknad.jobbHosNåværendeArbeidsgiver,
            arbeidsgivere = søknad.arbeidsgivere,
            utbetalingsperioder = søknad.utbetalingsperioder,
            andreUtbetalinger = søknad.andreUtbetalinger,
            fosterbarn = søknad.fosterbarn,
            bekreftelser = søknad.bekreftelser,
            vedlegg = vedlegg
        )

        omsorgpengesøknadMottakGateway.leggTilProsessering(
            soknad = komplettSoknad,
            callId = callId
        )

        logger.trace("Søknad lagt til prosessering. Sletter vedlegg.")

        vedleggService.slettVedleg(
            vedleggUrls = søknad.vedlegg,
            callId = callId,
            idToken = idToken
        )

        logger.trace("Vedlegg slettet.")

    }
}

