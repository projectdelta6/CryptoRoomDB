package com.duck.cryptoroomdb

import com.duck.cryptoroomdb.interfaces.Decryptor
import com.duck.cryptoroomdb.interfaces.Encryptor
import com.duck.cryptoroomdb.types.CryptoString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM unit tests for [CryptoString]. No Android runtime needed — the only `android.*`
 * reference is the `@RequiresApi` annotation constant, which is inlined at compile time.
 */
class CryptoStringTest {

    @Test
    fun defaultConstructor_isEmptyString() {
        val cs = CryptoString()
        assertEquals("", cs.value)
        assertEquals(0, cs.length)
    }

    @Test
    fun nullStringConstructor_isEmptyString() {
        assertEquals("", CryptoString(null as String?).value)
    }

    @Test
    fun valueConstructor_holdsValue() {
        val cs = CryptoString("hello")
        assertEquals("hello", cs.value)
        assertEquals(5, cs.length)
    }

    @Test
    fun copyConstructor_copiesValue() {
        assertEquals("source", CryptoString(CryptoString("source")).value)
    }

    @Test
    fun copyConstructor_withNull_isEmpty() {
        assertEquals("", CryptoString(null as CryptoString?).value)
    }

    @Test
    fun decryptorConstructor_decryptsValue() {
        val decryptor = object : Decryptor {
            override fun decrypt(encryptedValue: String): String = encryptedValue.removePrefix("enc:")
        }
        assertEquals("plain", CryptoString("enc:plain", decryptor).value)
    }

    @Test
    fun setValue_replacesValue() {
        val cs = CryptoString("old")
        cs.setValue(CryptoString("new"))
        assertEquals("new", cs.value)
    }

    @Test
    fun mutatingValue_updatesLength() {
        val cs = CryptoString("a")
        cs.value = "abcd"
        assertEquals(4, cs.length)
    }

    @Test
    fun plus_concatenatesStringRepresentation() {
        assertEquals("age:30", CryptoString("age:").plus(30))
    }

    @Test
    fun get_returnsCharAtIndex() {
        assertEquals('b', CryptoString("abc")[1])
    }

    @Test
    fun subSequence_returnsSlice() {
        assertEquals("bcd", CryptoString("abcde").subSequence(1, 4).toString())
    }

    @Test
    fun equals_withMatchingCryptoString_isTrue() {
        assertTrue(CryptoString("x") == CryptoString("x"))
        assertFalse(CryptoString("x") == CryptoString("y"))
    }

    @Test
    fun equals_withMatchingString_isTrue() {
        // Passing the String as Any? resolves to equals(Any?), hitting its String branch.
        // (`==` won't compile between the two unrelated final types.)
        assertTrue(CryptoString("x").equals("x" as Any?))
        assertFalse(CryptoString("x").equals("y" as Any?))
    }

    @Test
    fun equals_withUnrelatedType_isFalse() {
        assertFalse(CryptoString("1").equals(1))
        assertFalse(CryptoString("x").equals(null))
    }

    @Test
    fun equalsStringOverload_comparesValue() {
        // Explicit String arg resolves to the equals(String?) overload.
        assertTrue(CryptoString("x").equals("x"))
        assertFalse(CryptoString("x").equals("y"))
        assertFalse(CryptoString("x").equals(null as String?))
    }

    @Test
    fun compareTo_cryptoString_ordersByValue() {
        assertEquals(0, CryptoString("a").compareTo(CryptoString("a")))
        assertTrue(CryptoString("a") < CryptoString("b"))
        assertTrue(CryptoString("b") > CryptoString("a"))
    }

    @Test
    fun compareTo_string_ordersByValue() {
        assertEquals(0, CryptoString("a").compareTo("a"))
        assertTrue(CryptoString("a").compareTo("b") < 0)
    }

    @Test
    fun hashCode_matchesUnderlyingString() {
        assertEquals("hello".hashCode(), CryptoString("hello").hashCode())
    }

    @Test
    fun equalInstances_shareHashCode() {
        assertEquals(CryptoString("k").hashCode(), CryptoString("k").hashCode())
    }

    @Test
    fun toString_returnsValue() {
        assertEquals("printme", CryptoString("printme").toString())
    }

    @Test
    fun chars_streamsCharacters() {
        val count = CryptoString("abc").chars().count()
        assertEquals(3L, count)
    }

    @Test
    fun codePoints_streamsCodePoints() {
        val count = CryptoString("abc").codePoints().count()
        assertEquals(3L, count)
    }

    @Test
    fun encrypt_delegatesToEncryptor() {
        val encryptor = object : Encryptor {
            override fun encrypt(plainValue: String): String = "enc:$plainValue"
        }
        val encrypted = CryptoString("secret").encrypt(encryptor)
        assertEquals("enc:secret", encrypted)
        assertNotEquals("secret", encrypted)
    }
}
