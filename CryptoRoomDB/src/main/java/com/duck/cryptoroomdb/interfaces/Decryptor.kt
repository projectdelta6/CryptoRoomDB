package com.duck.cryptoroomdb.interfaces

/**
 * Created by Bradley Duck on 2018/10/19.
 */
interface Decryptor {
    /**
     * Should do the decrypting of the provided [String] value
     *
     *
     * Valid if: Decryptor.decrypt ([Encryptor.encrypt] (aString)) == aString;
     *
     * @param encryptedValue [String] the value to be decrypted.
     * @return The decrypted [String].
     */
    fun decrypt(encryptedValue: String): String
}