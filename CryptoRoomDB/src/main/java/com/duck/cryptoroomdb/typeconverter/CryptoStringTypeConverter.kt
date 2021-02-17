package com.duck.cryptoroomdb.typeconverter

import androidx.room.TypeConverter
import com.duck.cryptoroomdb.CryptoRoomDatabase
import com.duck.cryptoroomdb.types.CryptoString

/**
 * Created by Bradley Duck on 2018/10/19.
 *
 * [TypeConverter][androidx.room.TypeConverter] for the [CryptoString] object.<br></br>
 * This is where the actual encryption and decryption happens:<br></br>
 * * The value is encrypted when writing into the DB.<br></br>
 * * The value is decrypted when read out of the DB.
 */
class CryptoStringTypeConverter {

    @TypeConverter
    fun encrypt(_cryptoString: CryptoString?): String {
        var cryptoString = _cryptoString
        if (cryptoString == null) {
            cryptoString = CryptoString()
        }
        return cryptoString.encrypt(CryptoRoomDatabase.getEncryptor())
    }

    @TypeConverter
    fun decrypt(encryptedValue: String?): CryptoString {
        return if (encryptedValue == null) {
            CryptoString()
        } else CryptoString(encryptedValue, CryptoRoomDatabase.getDecryptor())
    }
}
