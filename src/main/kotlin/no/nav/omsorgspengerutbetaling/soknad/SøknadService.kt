package no.nav.omsorgspengerutbetaling.soknad

import no.nav.omsorgspengerutbetaling.felles.formaterStatuslogging
import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.auth.IdToken
import no.nav.omsorgspengerutbetaling.k9format.tilKOmsorgspengerUtbetalingSøknad
import no.nav.omsorgspengerutbetaling.soker.SøkerService
import no.nav.omsorgspengerutbetaling.soker.validate
import no.nav.omsorgspengerutbetaling.vedlegg.DokumentEier
import no.nav.omsorgspengerutbetaling.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime

internal class SøknadService(
    private val omsorgpengesøknadMottakGateway: OmsorgpengesøknadMottakGateway,
    private val vedleggService: VedleggService,
    private val søkerService: SøkerService
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SøknadService::class.java)
    }

    internal suspend fun registrer(
        søknad: Søknad,
        idToken: IdToken,
        callId: CallId,
    ) {
        logger.info(formaterStatuslogging(søknad.søknadId.id, "registreres"))

        val søker = søkerService.getSoker(idToken, callId)
        søker.validate()

        logger.info("Mapper om søknad til k9format.")
        val k9FormatSøknad = søknad.tilKOmsorgspengerUtbetalingSøknad(
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            søker = søker
        )
        søknad.valider(k9FormatSøknad)

        logger.trace("Henter ${søknad.vedlegg?.size ?: 0} vedlegg.")
        val vedlegg = vedleggService.hentVedlegg(
            idToken = idToken,
            vedleggUrls = søknad.vedlegg ?: listOf(),
            callId = callId,
            eier = DokumentEier(søker.fødselsnummer)
        )

        logger.trace("${vedlegg.size} vedlegg hentet. Validerer dem.")
        vedlegg.valider(vedleggReferanser = søknad.vedlegg ?: listOf())

        logger.info("Legger søknad til prosessering")
        val komplettSøknad = søknad.tilKomplettSøknad(k9FormatSøknad, søker, vedlegg)

        omsorgpengesøknadMottakGateway.leggTilProsessering(
            soknad = komplettSøknad,
            callId = callId
        )

        logger.trace("Søknad lagt til prosessering.")
    }
}