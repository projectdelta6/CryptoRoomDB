package com.duck.cryptoroomdb.roomtest

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface TestDao {

    @Upsert
    suspend fun upsert(entity: TestEntity)

    @Query("SELECT * FROM test_entity WHERE id = :id")
    suspend fun get(id: Int): TestEntity?

    /**
     * Reads the [TestEntity.secret] column straight out of SQLite as text, bypassing the
     * [CryptoString][com.duck.cryptoroomdb.types.CryptoString] type converter — so tests can
     * assert the value is actually stored encrypted (not as plain text).
     */
    @Query("SELECT secret FROM test_entity WHERE id = :id")
    suspend fun getRawSecret(id: Int): String?
}
