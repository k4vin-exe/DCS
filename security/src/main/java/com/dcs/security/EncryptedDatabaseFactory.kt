package com.dcs.security

import android.content.Context
import net.sqlcipher.database.SupportFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creates a SQLCipher [SupportFactory] using the Keystore-managed passphrase.
 * This factory is passed to Room's databaseBuilder to enable transparent encryption.
 */
@Singleton
class EncryptedDatabaseFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyManager: DatabaseKeyManager
) {
    fun createFactory(): SupportFactory {
        net.sqlcipher.database.SQLiteDatabase.loadLibs(context)
        val passphrase = keyManager.getOrCreatePassphrase()
        return SupportFactory(passphrase)
    }
}
