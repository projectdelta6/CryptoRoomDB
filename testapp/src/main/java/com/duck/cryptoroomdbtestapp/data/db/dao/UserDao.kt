package com.duck.cryptoroomdbtestapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.duck.cryptoroomdbtestapp.data.db.entity.EncryptedUserModel
import com.duck.cryptoroomdbtestapp.data.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao : BaseDao<UserEntity> {
    @Query("SELECT * FROM UserEntity WHERE id = :userId")
    suspend fun getUser(userId: Int): UserEntity?

    @Query("SELECT * FROM UserEntity WHERE id = :userId")
    fun getUserFlow(userId: Int): Flow<UserEntity?>

    @Query("SELECT * FROM UserEntity")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM UserEntity")
    fun getAllEncryptedUserModelFlow(): Flow<List<EncryptedUserModel>>

    @Query("DELETE FROM UserEntity")
    suspend fun deleteAll()
}
