package com.duck.cryptoroomdbtestapp.data.db

import android.content.Context
import android.util.Base64
import com.duck.cryptoroomdb.interfaces.Decryptor
import com.duck.cryptoroomdb.interfaces.Encryptor
import com.duck.cryptoroomdbtestapp.BuildConfig
import com.duck.cryptoroomdbtestapp.Log
import com.duck.cryptoroomdbtestapp.data.prefs.PrefsHelper
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Cryptor(context: Context) : Encryptor, Decryptor {
    var secretKey: SecretKeySpec
        private set
    var initialisationVectorSpec: IvParameterSpec
        private set

    init {
        if (isSecretKeyEmpty(context) && isInitVectorEmpty(context)) {
            this.initialisationVectorSpec = generateInitializationVector()
            this.secretKey = generateKey()
            saveInitVector(context)
            saveSecretKey(context)
        } else {
            this.initialisationVectorSpec = getInitializationVector(context)
            this.secretKey = getKey(context)
        }
    }

    override fun encrypt(plainValue: String): String {
        val data = encrypt(plainValue.toByteArray())
        if (data == null) {
            throw IllegalStateException("Encryption failed: Unable to encrypt the provided data.")
        }
        val encryptedString = Base64.encodeToString(data, Base64.DEFAULT)
        return encryptedString
    }

    private fun encrypt(dataToEncrypt: ByteArray): ByteArray? {
        var encryptedData: ByteArray? = null
        try {
            val cipher = Cipher.getInstance(AES_GCM_NO_PADDING)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, initialisationVectorSpec)
            encryptedData = cipher.doFinal(dataToEncrypt)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(this, "Error in encrypt: dataToEncrypt=\"$dataToEncrypt\"", e)
            } else {
                Log.e(this, "Error in encrypt", e)
            }
        }
        return encryptedData
    }

    override fun decrypt(encryptedValue: String): String {
        val encryptedBytes = Base64.decode(encryptedValue, Base64.DEFAULT)
        val decrypt = decrypt(encryptedBytes)
        val decryptedString = String(decrypt ?: byteArrayOf())
        return decryptedString
    }

    fun decrypt(encryptedData: ByteArray): ByteArray? {
        var decipheredBytesOfData: ByteArray? = null
        try {
            val cipher = Cipher.getInstance(AES_GCM_NO_PADDING)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, initialisationVectorSpec)
            decipheredBytesOfData = cipher.doFinal(encryptedData)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(this, "Error in decrypt: encryptedData\"$encryptedData\"", e)
            } else {
                Log.e(this, "Error in decrypt", e)
            }
        }
        return decipheredBytesOfData
    }

    private fun isSecretKeyEmpty(context: Context): Boolean {
        return PrefsHelper(context).key.isEmpty()
    }

    private fun isInitVectorEmpty(context: Context): Boolean {
        return PrefsHelper(context).iv.isEmpty()
    }

    private fun saveInitVector(context: Context) {
        PrefsHelper(context).iv = Base64.encodeToString(initialisationVectorSpec.iv, Base64.DEFAULT)
    }

    private fun saveSecretKey(context: Context) {
        PrefsHelper(context).key = Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
    }

    private fun getInitializationVector(context: Context): IvParameterSpec {
        val base64EncodedIvString = PrefsHelper(context).iv
        val ivBytes = Base64.decode(base64EncodedIvString, Base64.DEFAULT)
        return IvParameterSpec(ivBytes)
    }

    private fun getKey(context: Context): SecretKeySpec {
        val base64EncodedKeyString = PrefsHelper(context).key
        val keyBytes = Base64.decode(base64EncodedKeyString, Base64.DEFAULT)
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun generateInitializationVector(): IvParameterSpec {
        val secureRandom = SecureRandom()
        val ivString = ByteArray(16)
        secureRandom.nextBytes(ivString)
        return IvParameterSpec(ivString)
    }

    private fun generateKey(): SecretKeySpec {
        val secureRandom = SecureRandom()
        val key = ByteArray(16)
        secureRandom.nextBytes(key)
        return SecretKeySpec(key, "AES")
    }

    companion object {
        private const val AES_GCM_NO_PADDING = "AES/GCM/NoPadding"
    }
}