package com.duck.cryptoroomdb.typeconverter

import androidx.room.TypeConverter
import com.duck.cryptoroomdb.CryptoRoomDatabase
import com.duck.cryptoroomdb.types.CryptoString
import com.duck.cryptoroomdb.types.decryptToCryptoString
import com.duck.cryptoroomdb.types.encrypt

/**
 * Room [TypeConverter] for [CryptoString]. This is where the actual encryption happens:
 * * writing to the DB encrypts the plain value;
 * * reading from the DB decrypts the stored value.
 *
 * Registering this converter is mandatory — without it Room cannot persist a [CryptoString]
 * field and the build fails (by design; see [CryptoString]).
 */
class CryptoStringTypeConverter {

    @TypeConverter
    fun fromCryptoString(crypto: CryptoString): String =
        crypto.encrypt(CryptoRoomDatabase.getEncryptor())

    @TypeConverter
    fun toCryptoString(encrypted: String): CryptoString =
        encrypted.decryptToCryptoString(CryptoRoomDatabase.getDecryptor())
}
