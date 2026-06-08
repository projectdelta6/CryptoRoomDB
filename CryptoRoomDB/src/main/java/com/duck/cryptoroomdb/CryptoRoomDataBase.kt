package com.duck.cryptoroomdb

import androidx.room.RoomDatabase
import com.duck.cryptoroomdb.exceptions.DecryptorNotInitializedException
import com.duck.cryptoroomdb.exceptions.EncryptorNotInitializedException
import com.duck.cryptoroomdb.interfaces.Decryptor
import com.duck.cryptoroomdb.interfaces.Encryptor

/**
 * Base class for [RoomDatabase]s with field-level encryption.
 *
 * Store sensitive fields as [CryptoString][com.duck.cryptoroomdb.types.CryptoString] in your
 * `@Entity`s; they are encrypted on write and decrypted on read by
 * [CryptoStringTypeConverter][com.duck.cryptoroomdb.typeconverter.CryptoStringTypeConverter].
 *
 * To use it:
 * 1. Extend this class for your `@Database`.
 * 2. Register the converter on **your** `@Database` with
 *    `@TypeConverters(CryptoStringTypeConverter::class)`. Room does **not** inherit
 *    `@TypeConverters` from a superclass, so declaring it here would do nothing — it must be on
 *    your own `@Database` (and a [CryptoString][com.duck.cryptoroomdb.types.CryptoString] field
 *    won't compile without it).
 * 3. Call [setCryptoHelpers] during initialisation, before any database access.
 */
abstract class CryptoRoomDatabase : RoomDatabase() {

    /**
     * This needs to be called as part of the initialisation of the CryptoRoomDatabase implementation class.
     */
    fun setCryptoHelpers(_encryptor: Encryptor, _decryptor: Decryptor) {
        encryptor = _encryptor
        decryptor = _decryptor
    }

    companion object {

        private var encryptor: Encryptor? = null
        private var decryptor: Decryptor? = null

        internal fun getEncryptor(): Encryptor =
            encryptor ?: throw EncryptorNotInitializedException()

        internal fun getDecryptor(): Decryptor =
            decryptor ?: throw DecryptorNotInitializedException()
    }
}