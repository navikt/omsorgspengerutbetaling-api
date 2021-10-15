package no.nav.omsorgspengerutbetaling.soknad

import no.nav.omsorgspengerutbetaling.felles.formaterStatuslogging
import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.auth.IdToken
import no.nav.omsorgspengerutbetaling.k9format.tilKOmsorgspengerUtbetalingSøknad
import no.nav.omsorgspengerutbetaling.kafka.KafkaProducer
import no.nav.omsorgspengerutbetaling.kafka.Metadata
import no.nav.omsorgspengerutbetaling.soker.SøkerService
import no.nav.omsorgspengerutbetaling.soker.validate
import no.nav.omsorgspengerutbetaling.vedlegg.DokumentEier
import no.nav.omsorgspengerutbetaling.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.ZoneOffset
import java.time.ZonedDateTime

internal class SøknadService(
    private val vedleggService: VedleggService,
    private val søkerService: SøkerService,
    private val k9MellomlagringIngress: URI,
    private val kafkaProducer: KafkaProducer
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SøknadService::class.java)
    }

    internal suspend fun registrer(
        søknad: Søknad,
        metadata: Metadata,
        idToken: IdToken,
        callId: CallId,
    ) {
        logger.info(formaterStatuslogging(søknad.søknadId.id, "registreres"))

        val søker = søkerService.getSoker(idToken, callId)
        søker.validate()

        logger.info("Mapper om søknad til K9-format.")
        val k9FormatSøknad = søknad.tilKOmsorgspengerUtbetalingSøknad(
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            søker = søker
        )
        søknad.valider(k9FormatSøknad)

        if(søknad.vedlegg.isNotEmpty()){
            logger.info("Validerer ${søknad.vedlegg.size} vedlegg")
            val dokumentEier = DokumentEier(søker.fødselsnummer)
            val vedleggHentet = vedleggService.hentVedlegg(søknad.vedlegg, idToken, callId, dokumentEier)
            vedleggHentet.valider(søknad.vedlegg)

            logger.info("Persisterer vedlegg")
            vedleggService.persisterVedlegg(søknad.vedlegg, callId, dokumentEier)
        }

        logger.info("Legger søknad til prosessering")
        val komplettSøknad = søknad.tilKomplettSøknad(k9FormatSøknad, søker, k9MellomlagringIngress)

        try {
            kafkaProducer.produserKafkaMelding(komplettSøknad, metadata)
            return
        } catch (exception: Exception){
            logger.info("Feilet ved å legge melding på Kafka.")
            if(søknad.vedlegg.isNotEmpty()){
                logger.info("Fjerner hold på persisterte vedlegg")
                vedleggService.fjernHoldPåPersistertVedlegg(søknad.vedlegg, callId, DokumentEier(søker.fødselsnummer))
            }
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }
    }
}

class MeldingRegistreringFeiletException(s: String) : Throwable(s)