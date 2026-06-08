package com.duck.cryptoroomdb

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duck.cryptoroomdb.roomtest.CryptoTestSupport
import com.duck.cryptoroomdb.roomtest.FakeCryptor
import com.duck.cryptoroomdb.typeconverter.CryptoStringTypeConverter
import com.duck.cryptoroomdb.types.CryptoString
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises [CryptoStringTypeConverter]. The converter reaches the crypto helpers through
 * [CryptoRoomDatabase]'s static state, which we populate directly so no database instance is
 * required. Robolectric is used only so loading [CryptoRoomDatabase] (a
 * [androidx.room.RoomDatabase] subclass) is safe on the JVM.
 */
@RunWith(AndroidJUnit4::class)
class CryptoStringTypeConverterTest {

    private val cryptor = FakeCryptor()
    private val converter = CryptoStringTypeConverter()

    @Before
    fun setUp() {
        CryptoTestSupport.setHelpers(cryptor, cryptor)
    }

    @After
    fun tearDown() {
        CryptoTestSupport.reset()
    }

    @Test
    fun fromCryptoString_encrypts() {
        assertEquals(cryptor.encrypt("hello"), converter.fromCryptoString(CryptoString("hello")))
    }

    @Test
    fun toCryptoString_decrypts() {
        assertEquals("world", converter.toCryptoString(cryptor.encrypt("world")).value)
    }

    @Test
    fun roundTrip_returnsOriginal() {
        val original = CryptoString("secret")
        val restored = converter.toCryptoString(converter.fromCryptoString(original))
        assertEquals(original, restored)
    }
}
