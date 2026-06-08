package com.duck.cryptoroomdb

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duck.cryptoroomdb.exceptions.DecryptorNotInitializedException
import com.duck.cryptoroomdb.exceptions.EncryptorNotInitializedException
import com.duck.cryptoroomdb.roomtest.CryptoTestSupport
import com.duck.cryptoroomdb.roomtest.FakeCryptor
import com.duck.cryptoroomdb.roomtest.TestCryptoDatabase
import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests the static encryptor/decryptor lifecycle on [CryptoRoomDatabase]: the not-initialized
 * guards, and that `setCryptoHelpers` makes them retrievable.
 */
@RunWith(AndroidJUnit4::class)
class CryptoRoomDatabaseTest {

    private var db: TestCryptoDatabase? = null

    @Before
    fun setUp() {
        // Guarantee a clean slate regardless of test ordering / classloader reuse.
        CryptoTestSupport.reset()
    }

    @After
    fun tearDown() {
        db?.close()
        CryptoTestSupport.reset()
    }

    @Test
    fun getEncryptor_whenNotInitialized_throws() {
        assertThrows(EncryptorNotInitializedException::class.java) {
            CryptoRoomDatabase.getEncryptor()
        }
    }

    @Test
    fun getDecryptor_whenNotInitialized_throws() {
        assertThrows(DecryptorNotInitializedException::class.java) {
            CryptoRoomDatabase.getDecryptor()
        }
    }

    @Test
    fun setCryptoHelpers_makesHelpersRetrievable() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val encryptor = FakeCryptor()
        val decryptor = FakeCryptor()
        db = Room.inMemoryDatabaseBuilder(context, TestCryptoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            .apply { setCryptoHelpers(encryptor, decryptor) }

        assertSame(encryptor, CryptoRoomDatabase.getEncryptor())
        assertSame(decryptor, CryptoRoomDatabase.getDecryptor())
    }
}
