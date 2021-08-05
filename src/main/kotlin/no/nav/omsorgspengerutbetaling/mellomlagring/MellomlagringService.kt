package no.nav.omsorgspengerutbetaling.mellomlagring

import no.nav.omsorgspengerutbetaling.redis.RedisStore
import java.util.*

class MellomlagringService constructor(
    private val redisStore: RedisStore,
    private val passphrase: String
) {
    private val nøkkelPrefiks = "mellomlagring_"

    fun getMellomlagring(
        fnr: String
    ): String? {
        val krypto = Krypto(passphrase, fnr)
        val encrypted = redisStore.get(nøkkelPrefiks + fnr) ?: return null
        return krypto.decrypt(encrypted)
    }

    fun setMellomlagring(
        fnr: String,
        midlertidigSøknad: String,
        expirationDate: Date = Calendar.getInstance().let {
            it.add(Calendar.HOUR, 24)
            it.time
        }
    ) {
        val krypto = Krypto(passphrase, fnr)
        redisStore.set(nøkkelPrefiks + fnr, krypto.encrypt(midlertidigSøknad), expirationDate)
    }

    fun updateMellomlagring(
        fnr: String,
        midlertidigSøknad: String
    ) {
        val krypto = Krypto(passphrase, fnr)
        redisStore.update(nøkkelPrefiks + fnr, krypto.encrypt(midlertidigSøknad))
    }

    fun deleteMellomlagring(
        fnr: String
    ) {
        redisStore.delete(nøkkelPrefiks + fnr)
    }

    fun getTTLInMs(fnr: String): Long = redisStore.getPTTL(nøkkelPrefiks + fnr)
}