package com.dcs.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the encryption key for the SQLCipher Room database.
 *
 * Strategy:
 * 1. Generate a random 256-bit passphrase
 * 2. Encrypt it using an AES/GCM key stored in Android Keystore
 * 3. Store the encrypted passphrase in private SharedPreferences
 * 4. Decrypt on each app launch to unlock the database
 *
 * The raw passphrase never touches disk — only the encrypted version is persisted.
 */
@Singleton
class DatabaseKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "dcs_db_key"
        private const val PREFS_NAME = "dcs_security_prefs"
        private const val ENCRYPTED_PASSPHRASE_KEY = "encrypted_db_passphrase"
        private const val IV_KEY = "db_passphrase_iv"
        private const val GCM_TAG_LENGTH = 128
        private const val PASSPHRASE_LENGTH = 32
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Returns the database passphrase, creating one if it doesn't exist.
     * The passphrase is decrypted from Keystore-encrypted storage.
     */
    fun getOrCreatePassphrase(): ByteArray {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            createKeystoreKey()
        }

        val encryptedData = prefs.getString(ENCRYPTED_PASSPHRASE_KEY, null)
        val ivData = prefs.getString(IV_KEY, null)

        return if (encryptedData != null && ivData != null) {
            decryptPassphrase(
                Base64.decode(encryptedData, Base64.NO_WRAP),
                Base64.decode(ivData, Base64.NO_WRAP)
            )
        } else {
            val passphrase = generateRandomPassphrase()
            encryptAndStorePassphrase(passphrase)
            passphrase
        }
    }

    private fun createKeystoreKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(spec)
        keyGenerator.generateKey()
    }

    private fun generateRandomPassphrase(): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_LENGTH)
        SecureRandom().nextBytes(passphrase)
        return passphrase
    }

    private fun encryptAndStorePassphrase(passphrase: ByteArray) {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val key = keyStore.getKey(KEY_ALIAS, null) as SecretKey

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val encrypted = cipher.doFinal(passphrase)
        val iv = cipher.iv

        prefs.edit()
            .putString(ENCRYPTED_PASSPHRASE_KEY, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(IV_KEY, Base64.encodeToString(iv, Base64.NO_WRAP))
            .apply()
    }

    private fun decryptPassphrase(encrypted: ByteArray, iv: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val key = keyStore.getKey(KEY_ALIAS, null) as SecretKey

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        return cipher.doFinal(encrypted)
    }
}
