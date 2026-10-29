package io.github.cixweb.secretvault.data

import io.github.cixweb.secretvault.crypto.AeadProvider
import java.io.File
import kotlinx.coroutines.CoroutineScope

/** The encrypted store with the files and the migration around it. */
class SecretVault(
    val store: SecretStore,
    val encryptedFile: File,
    val plaintextFile: File,
    val migration: PlaintextMigration,
) {
    companion object {
        fun create(dir: File, aeadProvider: AeadProvider, scope: CoroutineScope): SecretVault {
            val encrypted = File(dir, SecretStore.ENCRYPTED_FILE_NAME)
            val plaintext = File(dir, SecretStore.PLAINTEXT_FILE_NAME)
            val migration = PlaintextMigration(plaintext)
            val store = SecretStore.encrypted(
                file = encrypted,
                aead = aeadProvider.aead(),
                scope = scope,
                migrations = listOf(migration),
            )
            return SecretVault(store, encrypted, plaintext, migration)
        }
    }
}
