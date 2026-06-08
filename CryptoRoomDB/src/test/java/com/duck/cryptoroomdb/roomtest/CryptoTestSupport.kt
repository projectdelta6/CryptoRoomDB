package com.duck.cryptoroomdb.roomtest

import com.duck.cryptoroomdb.CryptoRoomDatabase
import com.duck.cryptoroomdb.interfaces.Decryptor
import com.duck.cryptoroomdb.interfaces.Encryptor

/**
 * Test plumbing for the static crypto helpers held by [CryptoRoomDatabase].
 *
 * The encryptor/decryptor live as private static state on [CryptoRoomDatabase] and are normally
 * populated by `setCryptoHelpers(..)` on a database instance. For unit tests that exercise the
 * converter in isolation — and to guarantee a clean slate for the "not initialized" cases
 * regardless of Robolectric classloader reuse — we set/clear them directly by reflection.
 */
object CryptoTestSupport {

    private fun field(name: String) =
        CryptoRoomDatabase::class.java.getDeclaredField(name).apply { isAccessible = true }

    fun setHelpers(encryptor: Encryptor?, decryptor: Decryptor?) {
        field("encryptor").set(null, encryptor)
        field("decryptor").set(null, decryptor)
    }

    /** Clears both helpers so [CryptoRoomDatabase.getEncryptor]/[getDecryptor] throw again. */
    fun reset() = setHelpers(null, null)
}
