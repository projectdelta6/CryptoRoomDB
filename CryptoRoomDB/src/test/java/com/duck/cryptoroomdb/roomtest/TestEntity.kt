package com.duck.cryptoroomdb.roomtest

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.duck.cryptoroomdb.types.CryptoString

/**
 * Minimal entity for exercising the [CryptoString][com.duck.cryptoroomdb.types.CryptoString]
 * encryption round-trip through Room. [plain] is stored as-is; [secret] is encrypted on write
 * and decrypted on read by [CryptoStringTypeConverter][com.duck.cryptoroomdb.typeconverter.CryptoStringTypeConverter].
 */
@Entity(tableName = "test_entity")
data class TestEntity(
    @PrimaryKey val id: Int,
    val plain: String,
    val secret: CryptoString,
)
