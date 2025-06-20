package com.duck.cryptoroomdbtestapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.duck.cryptoroomdb.types.CryptoString
import com.duck.cryptoroomdbtestapp.data.db.AppDatabase
import com.duck.cryptoroomdbtestapp.data.db.entity.UserEntity
import com.duck.cryptoroomdbtestapp.data.model.UserDisplayModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app)
    private val userDao = db.userDao

    val normalUsers = userDao.getAllUsersFlow()
    val encryptedUsers = userDao.getAllEncryptedUserModelFlow()

    val users: StateFlow<List<UserDisplayModel>> =
        combine(normalUsers, encryptedUsers) { normalUsers, encryptedUsers ->
            normalUsers.map { user ->
                UserDisplayModel(
                    id = user.id,
                    name = user.name,
                    email = user.email,
                    age = user.age,
                    decryptedSecret = user.secret.value,
                    encryptedSecret = encryptedUsers.find { it.id == user.id }?.encryptedSecret
                        ?: ""
                )
            }

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addUser(name: String, email: String, age: Int, secret: String) {
        viewModelScope.launch {
            userDao.upsert(
                UserEntity(
                    name = name,
                    email = email,
                    age = age,
                    secret = CryptoString(secret)
                )
            )
        }
    }

    fun updateUser(id: Int, name: String, email: String, age: Int, secret: String) {
        viewModelScope.launch {
            userDao.upsert(
                UserEntity(
                    id = id,
                    name = name,
                    email = email,
                    age = age,
                    secret = CryptoString(secret)
                )
            )
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            val user = userDao.getUser(id)
            if (user != null) {
                userDao.delete(user)
            }
        }
    }
}

