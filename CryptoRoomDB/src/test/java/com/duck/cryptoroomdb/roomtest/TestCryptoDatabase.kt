package com.duck.cryptoroomdb.roomtest

import androidx.room.Database
import androidx.room.TypeConverters
import com.duck.cryptoroomdb.CryptoRoomDatabase
import com.duck.cryptoroomdb.typeconverter.CryptoStringTypeConverter

/**
 * Test-only concrete [CryptoRoomDatabase] used to drive the encryption round-trip on the JVM
 * under Robolectric. Lives in the test source set, so it is never part of the published library.
 */
@Database(entities = [TestEntity::class], version = 1, exportSchema = false)
@TypeConverters(CryptoStringTypeConverter::class)
abstract class TestCryptoDatabase : CryptoRoomDatabase() {
    abstract val testDao: TestDao
}
