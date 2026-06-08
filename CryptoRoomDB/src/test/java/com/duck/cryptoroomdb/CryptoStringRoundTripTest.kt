package com.duck.cryptoroomdb

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duck.cryptoroomdb.roomtest.CryptoTestSupport
import com.duck.cryptoroomdb.roomtest.FakeCryptor
import com.duck.cryptoroomdb.roomtest.TestCryptoDatabase
import com.duck.cryptoroomdb.roomtest.TestEntity
import com.duck.cryptoroomdb.types.CryptoString
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Full encryption round-trip through Room on the JVM (Robolectric, no emulator): a
 * [CryptoString] field is encrypted on write and decrypted on read by the type converter,
 * proving the converter + static helpers + Room integration all hang together.
 */
@RunWith(AndroidJUnit4::class)
class CryptoStringRoundTripTest {

    private lateinit var db: TestCryptoDatabase
    private val cryptor = FakeCryptor()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TestCryptoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            .apply { setCryptoHelpers(cryptor, cryptor) }
    }

    @After
    fun tearDown() {
        db.close()
        CryptoTestSupport.reset()
    }

    @Test
    fun insertThenRead_decryptsToOriginalValue() = runBlocking {
        db.testDao.upsert(TestEntity(id = 1, plain = "Alice", secret = CryptoString("topSecret")))

        val loaded = db.testDao.get(1)
        assertEquals("Alice", loaded?.plain)
        assertEquals("topSecret", loaded?.secret?.value)
    }

    @Test
    fun storedValue_isEncryptedNotPlainText() = runBlocking {
        db.testDao.upsert(TestEntity(id = 1, plain = "Alice", secret = CryptoString("topSecret")))

        val raw = db.testDao.getRawSecret(1)
        assertNotEquals("topSecret", raw)
        assertEquals(cryptor.encrypt("topSecret"), raw)
        assertTrue(raw!!.startsWith(FakeCryptor.PREFIX))
    }

    @Test
    fun emptySecret_roundTrips() = runBlocking {
        db.testDao.upsert(TestEntity(id = 2, plain = "Bob", secret = CryptoString("")))

        assertEquals("", db.testDao.get(2)?.secret?.value)
    }

    @Test
    fun updateSecret_persistsNewDecryptedValue() = runBlocking {
        db.testDao.upsert(TestEntity(id = 3, plain = "Carol", secret = CryptoString("old")))
        db.testDao.upsert(TestEntity(id = 3, plain = "Carol", secret = CryptoString("new")))

        assertEquals("new", db.testDao.get(3)?.secret?.value)
    }

    @Test
    fun read_missingRow_returnsNull() = runBlocking {
        assertNull(db.testDao.get(999))
    }
}
