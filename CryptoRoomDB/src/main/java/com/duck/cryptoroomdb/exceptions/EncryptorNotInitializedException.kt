package com.duck.cryptoroomdb.exceptions

/**
 * Created by Bradley Duck on 2018/10/19.
 */
class EncryptorNotInitializedException :
    RuntimeException("The 'encryptor' was not initialized! Did you forget call 'setCryptoHelpers(..)' on your CryptoRoomDatabase instance?")
