package no.nav.omsorgspengerutbetaling.barn

import com.github.benmanes.caffeine.cache.Cache
import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.auth.IdToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BarnService(
    private val barnGateway: BarnGateway,
    private val cache: Cache<String, List<BarnOppslag>>
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(BarnService::class.java)
    }

    internal suspend fun hentNåværendeBarn(
        idToken: IdToken,
        callId: CallId
    ): List<BarnOppslag>{
        var listeOverBarnOppslag = cache.getIfPresent(idToken.getSubject().toString())
        if(listeOverBarnOppslag != null) return listeOverBarnOppslag

        return try {
            val barnOppslag = barnGateway.hentBarn(
                idToken = idToken,
                callId = callId
            ).map{ it.tilBarnOppslag() }
            cache.put(idToken.getSubject().toString(), barnOppslag)
            barnOppslag
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av barn, returnerer en tom liste", cause)
            emptyList()
        }
    }

}