package com.duck.cryptoroomdbtestapp.data.db.dao

import androidx.room.Delete
import androidx.room.Upsert

interface BaseDao<E> {
    @Upsert
    suspend fun upsert(vararg entity: E)

    @Upsert
    suspend fun upsert(entities: List<E>)

    @Delete
    suspend fun delete(entity: E)
}
