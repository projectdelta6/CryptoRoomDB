package com.duck.cryptoroomdb.types

/**
 * Type-safe wrapper for strings that should be encrypted when stored in the database.
 *
 * This is a [value class][JvmInline] — compile-time type safety with zero runtime overhead;
 * at runtime it is just the wrapped `String`.
 *
 * **Why the backing property is `private`:** Room natively unwraps a value class that exposes a
 * persistable underlying type, which would map this straight to a `TEXT` column and **bypass
 * [CryptoStringTypeConverter][com.duck.cryptoroomdb.typeconverter.CryptoStringTypeConverter],
 * silently storing plaintext.** Hiding the backing property stops that: Room can no longer
 * persist the field on its own, so it *requires* the converter and fails to compile without it.
 * Read the plain value via [value]; construct with `CryptoString("...")`.
 *
 * The actual encryption/decryption happens in
 * [CryptoStringTypeConverter][com.duck.cryptoroomdb.typeconverter.CryptoStringTypeConverter].
 */
@JvmInline
value class CryptoString(private val raw: String) {

    /** The plain (decrypted) value held by this wrapper. */
    val value: String get() = raw

    override fun toString(): String = value

    companion object {
        /** A [CryptoString] wrapping the empty string. */
        val EMPTY = CryptoString("")
    }
}
