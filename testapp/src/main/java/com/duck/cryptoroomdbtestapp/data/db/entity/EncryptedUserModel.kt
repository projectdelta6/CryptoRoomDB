package com.duck.cryptoroomdbtestapp.data.db.entity

import androidx.room.ColumnInfo

data class EncryptedUserModel(
    val id: Int,
    @ColumnInfo(name = "secret")
    val encryptedSecret: String
)