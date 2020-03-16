package no.nav.omsorgspengerutbetaling.redis

import no.nav.omsorgspengerutbetaling.redis.RedisMockUtil.startRedisMocked

class RedisConfigurationProperties(private val redisMocked: Boolean) {

    fun startInMemoryRedisIfMocked() {
        if (redisMocked) {
            startRedisMocked()
        }
    }
}