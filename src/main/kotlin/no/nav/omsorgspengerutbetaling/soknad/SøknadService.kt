package no.nav.omsorgspengerutbetaling.soknad

import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.auth.IdToken
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

internal class SøknadService(
    private val omsorgpengesøknadMottakGateway: OmsorgpengesøknadMottakGateway,
    private val vedleggService: VedleggService
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SøknadService::class.java)
    }

    internal suspend fun registrer(
        søknad: Søknad,
        idToken: IdToken,
        callId: CallId,
        mottatt: ZonedDateTime,
        søker: Søker,
        k9FormatSøknad: no.nav.k9.søknad.Søknad
    ) {

        val utbetalingsperioder = søknad.utbetalingsperioder.map {
            UtbetalingsperiodeUtenVedlegg(
                fraOgMed = it.fraOgMed,
                tilOgMed = it.tilOgMed,
                lengde = it.lengde, //TODO: Fjerne etter prodsetting
                antallTimerBorte = it.antallTimerBorte,
                antallTimerPlanlagt = it.antallTimerPlanlagt,
                årsak = it.årsak
            )
        }

        logger.trace("Henter ${søknad.vedlegg?.size ?: 0} vedlegg.")
        val vedlegg = vedleggService.hentVedlegg(
            idToken = idToken,
            vedleggUrls = søknad.vedlegg ?: listOf(),
            callId = callId
        )

        logger.trace("${vedlegg.size} vedlegg hentet. Validerer dem.")
        vedlegg.valider(vedleggReferanser = søknad.vedlegg ?: listOf())

        logger.info("Legger søknad til prosessering")
        val komplettSoknad = KomplettSoknad(
            søknadId = søknad.søknadId,
            språk = søknad.språk,
            mottatt = mottatt,
            søker = søker,
            bosteder = søknad.bosteder,
            opphold = søknad.opphold,
            spørsmål = søknad.spørsmål,
            utbetalingsperioder = utbetalingsperioder,
            andreUtbetalinger = søknad.andreUtbetalinger,
            vedlegg = vedlegg,
            frilans = søknad.frilans,
            barn = søknad.barn,
            fosterbarn = søknad.fosterbarn,
            selvstendigVirksomheter = søknad.selvstendigVirksomheter,
            erArbeidstakerOgså = søknad.erArbeidstakerOgså,
            hjemmePgaSmittevernhensyn = søknad.hjemmePgaSmittevernhensyn,
            hjemmePgaStengtBhgSkole = søknad.hjemmePgaStengtBhgSkole,
            bekreftelser = søknad.bekreftelser,
            k9FormatSøknad = k9FormatSøknad
        )

        omsorgpengesøknadMottakGateway.leggTilProsessering(
            soknad = komplettSoknad,
            callId = callId
        )

        logger.trace("Søknad lagt til prosessering. Sletter vedlegg.")
        vedleggService.slettVedleg(
            vedleggUrls = søknad.vedlegg ?: listOf(),
            callId = callId,
            idToken = idToken
        )
        logger.trace("Vedlegg slettet.")
    }
}

