package com.koflox.cyclingassistant.data

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal interface DatabasePassphraseManager {
    fun getPassphrase(): ByteArray
}

internal class DatabasePassphraseManagerImpl(
    private val context: Context,
) : DatabasePassphraseManager {

    companion object {
        private const val KEYSTORE_ALIAS = "db_passphrase_key"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val PREFS_NAME = "db_passphrase_prefs"
        private const val KEY_ENCRYPTED_PASSPHRASE = "encrypted_passphrase"
        private const val KEY_IV = "passphrase_iv"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val PASSPHRASE_LENGTH = 32
    }

    override fun getPassphrase(): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encryptedBase64 = prefs.getString(KEY_ENCRYPTED_PASSPHRASE, null)
        val ivBase64 = prefs.getString(KEY_IV, null)
        return if (encryptedBase64 != null && ivBase64 != null) {
            recoverOrRegenerate(prefs, encryptedBase64, ivBase64)
        } else {
            generateAndStorePassphrase(prefs)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun recoverOrRegenerate(
        prefs: SharedPreferences,
        encryptedBase64: String,
        ivBase64: String,
    ): ByteArray = try {
        decryptPassphrase(
            encrypted = Base64.decode(encryptedBase64, Base64.NO_WRAP),
            iv = Base64.decode(ivBase64, Base64.NO_WRAP),
        )
    } catch (e: Exception) {
        clearStoredPassphrase(prefs)
        generateAndStorePassphrase(prefs)
    }

    private fun clearStoredPassphrase(prefs: SharedPreferences) {
        prefs.edit { remove(KEY_ENCRYPTED_PASSPHRASE).remove(KEY_IV) }
    }

    private fun generateAndStorePassphrase(prefs: SharedPreferences): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_LENGTH).apply {
            java.security.SecureRandom().nextBytes(this)
        }
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKeystoreKey())
        }
        val encrypted = cipher.doFinal(passphrase)
        prefs.edit {
            putString(KEY_ENCRYPTED_PASSPHRASE, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
        }
        return passphrase
    }

    private fun decryptPassphrase(encrypted: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getOrCreateKeystoreKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        }
        return cipher.doFinal(encrypted)
    }

    private fun getOrCreateKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existingKey = keyStore.getEntry(KEYSTORE_ALIAS, null)
        return if (existingKey is KeyStore.SecretKeyEntry) {
            existingKey.secretKey
        } else {
            createKeystoreKey()
        }
    }

    private fun createKeystoreKey(): SecretKey {
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER).apply {
            init(spec)
        }.generateKey()
    }
}
