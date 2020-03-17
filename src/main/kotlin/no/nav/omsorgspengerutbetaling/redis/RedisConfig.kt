package no.nav.omsorgspengerutbetaling.redis

import io.ktor.util.KtorExperimentalAPI
import io.lettuce.core.RedisClient

internal object RedisConfig {

    @KtorExperimentalAPI
    internal fun redisClient(redisHost: String, redisPort: Int): RedisClient {
        return RedisClient.create("redis://${redisHost}:${redisPort}")
    }

}