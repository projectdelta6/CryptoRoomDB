package com.duck.cryptoroomdbtestapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.duck.cryptoroomdb.types.CryptoString

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val age: Int,
    val secret: CryptoString
)
