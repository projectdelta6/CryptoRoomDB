package com.duck.cryptoroomdb.types

import android.os.Build
import androidx.annotation.RequiresApi
import com.duck.cryptoroomdb.interfaces.Decryptor
import com.duck.cryptoroomdb.interfaces.Encryptor
import java.util.stream.IntStream

/**
 * Created by Bradley Duck on 2018/10/19.
 *
 * String Wrapper class for the purpose of encrypting the value held within when writing into
 * the DB and decrypting when reading from the DB.
 */
class CryptoString(
    var value: String = ""
) : Comparable<CryptoString>, CharSequence {

    /**
     * Copy Constructor.
     */
    constructor(value: CryptoString) : this(value.value)

    /**
     * Decryptor Constructor.
     *
     * This will decrypt the [encryptedValue] using the [decryptor] and initialise a
     * new [CryptoString] with the decrypted value
     *
     * @param encryptedValue the encrypted [String] to be decrypted.
     * @param decryptor the [Decryptor] to be used to decrypt the [encryptedValue].
     */
    constructor(encryptedValue: String, decryptor: Decryptor) : this(
        decryptor.decrypt(
            encryptedValue
        )
    )

    /**
     * Returns the length of this character sequence.
     */
    override val length: Int by value::length

    /**
     * Sets the internal [String] value to match that of the provided [CryptoString].
     *
     * @param value [CryptoString] the value to be set.
     */
    fun setValue(value: CryptoString) {
        this.value = value.value
    }

    /**
     * Returns a string obtained by concatenating this string with the string representation of the given [other] object.
     */
    operator fun plus(other: Any?): String = value.plus(other)

    override operator fun get(index: Int): Char = value[index]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        value.subSequence(startIndex, endIndex)

    override fun equals(other: Any?): Boolean {
        if (other is CryptoString) {
            return value == other.value
        }
        return if (other is String) {
            value == other
        } else super.equals(other)
    }

    fun equals(other: String?): Boolean {
        return value == other
    }

    override operator fun compareTo(other: CryptoString): Int = compareTo(other.value)

    operator fun compareTo(other: String): Int = value.compareTo(other)

    override fun hashCode(): Int = value.hashCode()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun chars(): IntStream = value.chars()

    override fun toString(): String = value

    @RequiresApi(Build.VERSION_CODES.N)
    override fun codePoints(): IntStream = value.codePoints()

    /**
     * @param encryptor [Encryptor] to do the actual encryption.
     * @return The **encrypted** value of this CryptoString.
     */
    fun encrypt(encryptor: Encryptor): String = encryptor.encrypt(value)
}
