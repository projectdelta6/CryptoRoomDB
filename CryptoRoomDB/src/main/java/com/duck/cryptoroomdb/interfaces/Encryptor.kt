package com.duck.cryptoroomdb.interfaces

/**
 * Created by Bradley Duck on 2018/10/19.
 */
interface Encryptor {
    /**
     * Should do the Encrypting of the provided [String] value.
     *
     *
     * Valid if: [Decryptor.decrypt] (Encryptor.encrypt(aString)) == aString;
     *
     * @param plainValue [String] the value to be encrypted.
     * @return The encrypted [String].
     */
    fun encrypt(plainValue: String): String
}