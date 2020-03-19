package no.nav.omsorgspengerutbetaling.vedlegg

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.auth.IdToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

private val logger: Logger = LoggerFactory.getLogger("nav.VedleggService")

class VedleggService(
    private val k9DokumentGateway: K9DokumentGateway
) {
    suspend fun lagreVedlegg(
        vedlegg: Vedlegg,
        idToken: IdToken,
        callId: CallId
    ) : VedleggId {

        return k9DokumentGateway.lagreVedlegg(
            vedlegg = vedlegg,
            idToken = idToken,
            callId = callId
        )

    }

    suspend fun hentVedlegg(
        vedleggId: VedleggId,
        idToken: IdToken,
        callId: CallId
    ) : Vedlegg? {

        return k9DokumentGateway.hentVedlegg(
            vedleggId = vedleggId,
            idToken = idToken,
            callId = callId
        )
    }

    suspend fun hentVedlegg(
        vedleggUrls: List<URI>,
        idToken: IdToken,
        callId: CallId
    ) : List<Vedlegg> {
        val vedlegg = coroutineScope {
            val futures = mutableListOf<Deferred<Vedlegg?>>()
            vedleggUrls.forEach {
                futures.add(async { hentVedlegg(
                    vedleggId = vedleggIdFromUrl(it),
                    idToken = idToken,
                    callId = callId
                )})

            }
            futures.awaitAll().filterNotNull()
        }
        return vedlegg.requireNoNulls()
    }

    suspend fun slettVedleg(
        vedleggId: VedleggId,
        idToken: IdToken,
        callId: CallId
    ) {
        k9DokumentGateway.slettVedlegg(
            vedleggId = vedleggId,
            idToken = idToken,
            callId = callId
        )
    }

    suspend fun slettVedleg(
        vedleggUrls: List<URI>,
        idToken: IdToken,
        callId: CallId
    ) {
        coroutineScope {
            val futures = mutableListOf<Deferred<Unit>>()
            vedleggUrls.forEach {
                futures.add(async { slettVedleg(
                    vedleggId = vedleggIdFromUrl(it),
                    idToken = idToken,
                    callId = callId
                )})

            }
            futures.awaitAll()
        }
    }

    private fun vedleggIdFromUrl(url: URI) : VedleggId {
        return VedleggId(url.path.substringAfterLast("/"))
    }
}