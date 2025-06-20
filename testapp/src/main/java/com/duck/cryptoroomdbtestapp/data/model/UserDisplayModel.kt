package com.duck.cryptoroomdbtestapp.data.model

/**
 * Model for displaying user data in the UI, including both decrypted and encrypted secret.
 */
data class UserDisplayModel(
    val id: Int,
    val name: String,
    val email: String,
    val age: Int,
    val decryptedSecret: String,
    val encryptedSecret: String
)