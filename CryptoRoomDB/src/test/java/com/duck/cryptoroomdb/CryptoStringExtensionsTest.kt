package com.duck.cryptoroomdb

import com.duck.cryptoroomdb.interfaces.Decryptor
import com.duck.cryptoroomdb.interfaces.Encryptor
import com.duck.cryptoroomdb.types.CryptoString
import com.duck.cryptoroomdb.types.compareTo
import com.duck.cryptoroomdb.types.contentEquals
import com.duck.cryptoroomdb.types.decryptToCryptoString
import com.duck.cryptoroomdb.types.encrypt
import com.duck.cryptoroomdb.types.isBlank
import com.duck.cryptoroomdb.types.isEmpty
import com.duck.cryptoroomdb.types.isNotBlank
import com.duck.cryptoroomdb.types.isNotEmpty
import com.duck.cryptoroomdb.types.length
import com.duck.cryptoroomdb.types.toCryptoString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure-JVM unit tests for the [CryptoString] extension functions. */
class CryptoStringExtensionsTest {

    private val encryptor = object : Encryptor {
        override fun encrypt(plainValue: String): String = "enc:$plainValue"
    }
    private val decryptor = object : Decryptor {
        override fun decrypt(encryptedValue: String): String = encryptedValue.removePrefix("enc:")
    }

    @Test
    fun encrypt_delegatesToEncryptor() {
        assertEquals("enc:secret", CryptoString("secret").encrypt(encryptor))
    }

    @Test
    fun toCryptoString_wrapsString() {
        assertEquals("x", "x".toCryptoString().value)
    }

    @Test
    fun decryptToCryptoString_decryptsAndWraps() {
        assertEquals("plain", "enc:plain".decryptToCryptoString(decryptor).value)
    }

    @Test
    fun compareTo_ordersByUnderlyingValue() {
        assertEquals(0, CryptoString("a").compareTo(CryptoString("a")))
        assertTrue(CryptoString("a") < CryptoString("b"))
        assertTrue(CryptoString("b") > CryptoString("a"))
    }

    @Test
    fun contentEquals_comparesUnderlyingValue() {
        assertTrue(CryptoString("x").contentEquals("x"))
        assertFalse(CryptoString("x").contentEquals("y"))
    }

    @Test
    fun length_returnsUnderlyingLength() {
        assertEquals(3, CryptoString("abc").length)
        assertEquals(0, CryptoString.EMPTY.length)
    }

    @Test
    fun emptinessChecks() {
        assertTrue(CryptoString("").isEmpty())
        assertFalse(CryptoString("a").isEmpty())
        assertTrue(CryptoString("a").isNotEmpty())
        assertFalse(CryptoString("").isNotEmpty())
    }

    @Test
    fun blanknessChecks() {
        assertTrue(CryptoString("   ").isBlank())
        assertFalse(CryptoString("a").isBlank())
        assertTrue(CryptoString("a").isNotBlank())
        assertFalse(CryptoString("   ").isNotBlank())
    }
}
