package io.github.cixweb.secretvault.data

import io.github.cixweb.secretvault.crypto.AeadProvider
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The encrypted store with the files, the migration and the corruption reset around it.
 *
 * @property store the encrypted secret.
 * @property encryptedFile where [store] keeps the ciphertext.
 * @property plaintextFile the legacy file; it exists only until the migration has run.
 * @property migration tells whether the first read migrated a legacy value.
 * @property corruption tells whether the first read replaced a file that did not decrypt.
 */
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
         *
         * @param dir the directory of both files, normally `Context.filesDir`.
         * @param aeadProvider the key: the Keystore one on a device, an in-memory one in tests.
         * @param scope runs the DataStore; only one store per file may be active at a time.
         * @param ioDispatcher where the key is loaded; tests pass their own.
         * @throws java.security.GeneralSecurityException when the key cannot be created or used.
         * @throws java.io.IOException when the keyset cannot be read or written.
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
