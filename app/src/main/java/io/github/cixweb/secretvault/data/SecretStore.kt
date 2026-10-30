package io.github.cixweb.secretvault.data

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import com.google.crypto.tink.Aead
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * One secret string in a DataStore. The factories build the legacy plaintext store and the
 * encrypted one behind the same API.
 *
 * @param dataStore the store that holds the string.
 */
class SecretStore(private val dataStore: DataStore<String>) {
    /** The stored secret, then every change to it; empty when nothing is stored. */
    val secret: Flow<String> = dataStore.data

    /**
     * Replaces the stored secret with [value] and returns once it is on disk.
     *
     * @throws java.io.IOException when the file cannot be written.
     */
    suspend fun save(value: String) {
        dataStore.updateData { value }
    }

    /** Stores an empty secret; in the encrypted store the file then holds an empty ciphertext. */
    suspend fun clear() = save("")

    companion object {
        /** The file the legacy version wrote the secret to, in clear text. */
        const val PLAINTEXT_FILE_NAME = "secret.txt"

        /** The encrypted file; its name is also the associated data of every ciphertext. */
        const val ENCRYPTED_FILE_NAME = "secret.enc"

        /**
         * The legacy store: the value is written to disk as it is.
         *
         * @param file the plaintext file.
         * @param scope runs the DataStore; cancel it to close the store.
         */
        fun plaintext(file: File, scope: CoroutineScope): SecretStore =
            SecretStore(
                DataStoreFactory.create(
                    serializer = SecretSerializer,
                    scope = scope,
                    produceFile = { file },
                ),
            )

        /**
         * The current store: every write is encrypted with [aead], bound to the file name.
         * Without a [corruptionHandler], a file that does not decrypt fails every read.
         *
         * @param file the encrypted file; its name is bound to every ciphertext.
         * @param aead encrypts every write and decrypts every read.
         * @param scope runs the DataStore; cancel it to close the store.
         * @param migrations run once before the first read, such as [PlaintextMigration].
         * @param corruptionHandler replaces a file that does not decrypt, such as
         *   [CorruptionReset.handler].
         */
        fun encrypted(
            file: File,
            aead: Aead,
            scope: CoroutineScope,
            migrations: List<DataMigration<String>> = emptyList(),
            corruptionHandler: ReplaceFileCorruptionHandler<String>? = null,
        ): SecretStore =
            SecretStore(
                DataStoreFactory.create(
                    serializer = encryptedSecretSerializer(aead, file.name),
                    corruptionHandler = corruptionHandler,
                    scope = scope,
                    migrations = migrations,
                    produceFile = { file },
                ),
            )
    }
}
