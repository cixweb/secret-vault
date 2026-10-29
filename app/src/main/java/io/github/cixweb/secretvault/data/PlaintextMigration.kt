package io.github.cixweb.secretvault.data

import androidx.datastore.core.DataMigration
import java.io.File

enum class MigrationOutcome { NotRun, Migrated, NothingToMigrate }

/** Moves the value of the legacy plaintext file into the encrypted store, then deletes that file. */
class PlaintextMigration(private val plaintextFile: File) : DataMigration<String> {
    @Volatile
    var outcome: MigrationOutcome = MigrationOutcome.NotRun
        private set

    override suspend fun shouldMigrate(currentData: String): Boolean {
        val needed = plaintextFile.exists()
        if (!needed) outcome = MigrationOutcome.NothingToMigrate
        return needed
    }

    override suspend fun migrate(currentData: String): String {
        val legacy = plaintextFile.inputStream().use { SecretSerializer.readFrom(it) }
        return currentData.ifEmpty { legacy }
    }

    override suspend fun cleanUp() {
        plaintextFile.delete()
        outcome = MigrationOutcome.Migrated
    }
}
