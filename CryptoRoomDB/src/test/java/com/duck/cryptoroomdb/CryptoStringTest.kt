package com.duck.cryptoroomdb

import com.duck.cryptoroomdb.types.CryptoString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Pure-JVM unit tests for the [CryptoString] value class. Equality and hashCode are compiler
 * generated from the wrapped value; this verifies that contract plus [CryptoString.value],
 * [CryptoString.toString], and the [CryptoString.EMPTY] constant.
 */
class CryptoStringTest {

    @Test
    fun value_holdsWrappedString() {
        assertEquals("hello", CryptoString("hello").value)
    }

    @Test
    fun toString_returnsValue() {
        assertEquals("hello", CryptoString("hello").toString())
    }

    @Test
    fun equals_isValueBased() {
        assertEquals(CryptoString("a"), CryptoString("a"))
        assertNotEquals(CryptoString("a"), CryptoString("b"))
    }

    @Test
    fun hashCode_matchesForEqualValues() {
        assertEquals(CryptoString("k").hashCode(), CryptoString("k").hashCode())
    }

    @Test
    fun empty_wrapsEmptyString() {
        assertEquals("", CryptoString.EMPTY.value)
        assertEquals(CryptoString(""), CryptoString.EMPTY)
    }
}
