package com.duck.cryptoroomdb.exceptions

/**
 * Created by Bradley Duck on 2018/10/19.
 */
class DecryptorNotInitializedException :
    RuntimeException("The 'decryptor' was not initialized! Did you forget call 'setCryptoHelpers(..)' on your CryptoRoomDatabase instance?")
