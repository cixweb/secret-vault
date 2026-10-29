package io.github.cixweb.secretvault.data

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.google.crypto.tink.Aead
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/** One secret string in a DataStore. */
class SecretStore(private val dataStore: DataStore<String>) {
    val secret: Flow<String> = dataStore.data

    suspend fun save(value: String) {
        dataStore.updateData { value }
    }

    suspend fun clear() = save("")

    companion object {
        const val PLAINTEXT_FILE_NAME = "secret.txt"
        const val ENCRYPTED_FILE_NAME = "secret.enc"

        /** The legacy store: the value is written to disk as it is. */
        fun plaintext(file: File, scope: CoroutineScope): SecretStore =
            SecretStore(
                DataStoreFactory.create(
                    serializer = SecretSerializer,
                    scope = scope,
                    produceFile = { file },
                ),
            )

        /** The current store: every write is encrypted with [aead], bound to the file name. */
        fun encrypted(
            file: File,
            aead: Aead,
            scope: CoroutineScope,
            migrations: List<DataMigration<String>> = emptyList(),
        ): SecretStore =
            SecretStore(
                DataStoreFactory.create(
                    serializer = encryptedSecretSerializer(aead, file.name),
                    scope = scope,
                    migrations = migrations,
                    produceFile = { file },
                ),
            )
    }
}
