package com.duck.cryptoroomdbtestapp.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duck.cryptoroomdb.types.CryptoString
import com.duck.cryptoroomdbtestapp.data.db.entity.UserEntity
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build().apply {
                val cryptor = Cryptor(context)
                setCryptoHelpers(cryptor, cryptor)
            }
    }

    @After
    fun closeDb() {
        db.close()
    }

    /**
     * Inserts a user with an encrypted secret and verifies
     * that the decrypted value matches the original input.
     */
    @Test
    fun insertAndReadUser_withCryptoString() = runBlocking {
        val secretValue = "mySecretValue"
        val user = UserEntity(
            id = 1,
            name = "Alice",
            email = "alice@example.com",
            age = 30,
            secret = CryptoString(secretValue)
        )
        db.userDao.upsert(user)
        val loaded = db.userDao.getUser(1)
        assertEquals(user.id, loaded?.id)
        assertEquals(user.name, loaded?.name)
        assertEquals(user.email, loaded?.email)
        assertEquals(user.age, loaded?.age)
        assertEquals(secretValue, loaded?.secret?.value)
    }

    /**
     * Updates a user's encrypted secret and verifies
     * that the decrypted value is updated correctly.
     */
    @Test
    fun updateUserSecret_updatesDecryptedValue() = runBlocking {
        val user = UserEntity(1, "Bob", "bob@example.com", 25, CryptoString("oldSecret"))
        db.userDao.upsert(user)
        val updated = user.copy(secret = CryptoString("newSecret"))
        db.userDao.upsert(updated)
        val loaded = db.userDao.getUser(1)
        assertEquals("newSecret", loaded?.secret?.value)
    }

    /**
     * Deletes a user and ensures that the user is removed from the database.
     */
    @Test
    fun deleteUser_removesUser() = runBlocking {
        val user = UserEntity(2, "Carol", "carol@example.com", 28, CryptoString("carolSecret"))
        db.userDao.upsert(user)
        db.userDao.delete(user)
        val loaded = db.userDao.getUser(2)
        assertEquals(null, loaded)
    }

    /**
     * Inserts multiple users with different secrets and verifies
     * that each decrypted value matches the original input.
     */
    @Test
    fun insertMultipleUsers_andReadAll() = runBlocking {
        val users = listOf(
            UserEntity(3, "Dave", "dave@example.com", 40, CryptoString("daveSecret")),
            UserEntity(4, "Eve", "eve@example.com", 35, CryptoString("eveSecret"))
        )
        db.userDao.upsert(users)
        val dave = db.userDao.getUser(3)
        val eve = db.userDao.getUser(4)
        assertEquals("daveSecret", dave?.secret?.value)
        assertEquals("eveSecret", eve?.secret?.value)
    }

    /**
     * Inserts a user and verifies that the Flow emits the correct decrypted value.
     */
    @Test
    fun getUserFlow_emitsDecryptedValue() = runBlocking {
        val user = UserEntity(5, "Frank", "frank@example.com", 50, CryptoString("frankSecret"))
        db.userDao.upsert(user)
        val flow = db.userDao.getUserFlow(5)
        val first = flow
            .filterNotNull()
            .first()
        assertEquals("frankSecret", first.secret.value)
    }
}
