package no.nav.omsorgspengerutbetaling.redis

import io.ktor.util.KtorExperimentalAPI
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import java.time.Duration

internal object RedisConfig {

    @KtorExperimentalAPI
    internal fun redisClient(redisHost: String, redisPort: Int): RedisClient {
        val host = redisHost.resolveHost()
        return RedisClient.create(RedisURI(host, redisPort, Duration.ofSeconds(60)))
    }

    private fun String.resolveHost() = when {
        nonRoutableMetaAddress() -> replace("0.0.0.0", "localhost")
        else -> this
    }

    private fun String.nonRoutableMetaAddress(): Boolean = contains("0.0.0.0")
}
