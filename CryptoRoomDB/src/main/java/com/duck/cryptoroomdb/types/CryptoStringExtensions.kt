package com.duck.cryptoroomdb.types

import com.duck.cryptoroomdb.interfaces.Decryptor
import com.duck.cryptoroomdb.interfaces.Encryptor

/**
 * Encrypts this [CryptoString]'s value using [encryptor].
 * @return the encrypted string (ciphertext).
 */
fun CryptoString.encrypt(encryptor: Encryptor): String = encryptor.encrypt(value)

/** Wraps this plain [String] in a [CryptoString]. */
fun String.toCryptoString(): CryptoString = CryptoString(this)

/** Decrypts this encrypted [String] with [decryptor] and wraps the result in a [CryptoString]. */
fun String.decryptToCryptoString(decryptor: Decryptor): CryptoString =
    CryptoString(decryptor.decrypt(this))

/** Lexicographically compares the underlying values of two [CryptoString]s. */
operator fun CryptoString.compareTo(other: CryptoString): Int = value.compareTo(other.value)

/** Returns true if the underlying value equals the plain [other]. */
fun CryptoString.contentEquals(other: String): Boolean = value == other

/** The length of the underlying value. */
val CryptoString.length: Int get() = value.length

/** Returns true if the underlying value is empty. */
fun CryptoString.isEmpty(): Boolean = value.isEmpty()

/** Returns true if the underlying value is not empty. */
fun CryptoString.isNotEmpty(): Boolean = value.isNotEmpty()

/** Returns true if the underlying value is blank. */
fun CryptoString.isBlank(): Boolean = value.isBlank()

/** Returns true if the underlying value is not blank. */
fun CryptoString.isNotBlank(): Boolean = value.isNotBlank()
