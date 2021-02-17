package com.duck.cryptoroomdb

import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.duck.cryptoroomdb.exceptions.DecryptorNotInitializedException
import com.duck.cryptoroomdb.exceptions.EncryptorNotInitializedException
import com.duck.cryptoroomdb.interfaces.Decryptor
import com.duck.cryptoroomdb.interfaces.Encryptor
import com.duck.cryptoroomdb.typeconverter.CryptoStringTypeConverter

/**
 * Created by Bradley Duck on 2018/10/19.
 *
 * Wrapper class for [RoomDatabase] with Added DataEncryption through the use of the
 * [CryptoString][com.duck.cryptoroomdb.types.CryptoString] object in your [Entity](s) and
 * including the [CryptoStringTypeConverter] in your [TypeConverters]
 */
@TypeConverters(value = [CryptoStringTypeConverter::class])
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

        @Throws(EncryptorNotInitializedException::class)
        fun getEncryptor(): Encryptor {
            if (encryptor == null) {
                throw EncryptorNotInitializedException()
            }
            return encryptor as Encryptor
        }

        @Throws(DecryptorNotInitializedException::class)
        fun getDecryptor(): Decryptor {
            if (decryptor == null) {
                throw DecryptorNotInitializedException()
            }
            return decryptor as Decryptor
        }
    }
}