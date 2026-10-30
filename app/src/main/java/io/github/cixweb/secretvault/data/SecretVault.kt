package io.github.cixweb.secretvault.data

import io.github.cixweb.secretvault.crypto.AeadProvider
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The encrypted store with the files, the migration and the corruption reset around it. */
class SecretVault(
    val store: SecretStore,
    val encryptedFile: File,
    val plaintextFile: File,
    val migration: PlaintextMigration,
    val corruption: CorruptionReset,
) {
    companion object {
        /**
         * Opens the vault on [ioDispatcher]. Getting the Aead reads the keyset from disk and
         * asks the Android Keystore to decrypt it: never on the main thread.
         */
        suspend fun open(
            dir: File,
            aeadProvider: AeadProvider,
            scope: CoroutineScope,
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        ): SecretVault = withContext(ioDispatcher) {
            val encrypted = File(dir, SecretStore.ENCRYPTED_FILE_NAME)
            val plaintext = File(dir, SecretStore.PLAINTEXT_FILE_NAME)
            val migration = PlaintextMigration(plaintext)
            val corruption = CorruptionReset()
            val store = SecretStore.encrypted(
                file = encrypted,
                aead = aeadProvider.aead(),
                scope = scope,
                migrations = listOf(migration),
                corruptionHandler = corruption.handler,
            )
            SecretVault(store, encrypted, plaintext, migration, corruption)
        }
    }
}
