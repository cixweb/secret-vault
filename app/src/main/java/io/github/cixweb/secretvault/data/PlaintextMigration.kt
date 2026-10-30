package io.github.cixweb.secretvault.data

import androidx.datastore.core.DataMigration
import java.io.File

/** What the migration did since the process started. */
enum class MigrationOutcome {
    /** The store has not been read yet, so DataStore has not run the migration. */
    NotRun,

    /** A legacy file was copied into the encrypted store, then deleted. */
    Migrated,

    /** There was no legacy file. */
    NothingToMigrate,
}

/**
 * Moves the value of the legacy plaintext file into the encrypted store, then deletes that
 * file. DataStore runs it before the first read and calls [cleanUp] only after the migrated
 * value is written, so a crash in between loses nothing: the next start migrates again.
 *
 * @param plaintextFile the file an older version wrote the secret to.
 */
class PlaintextMigration(private val plaintextFile: File) : DataMigration<String> {
    /** What the migration did; final once the store has been read. */
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
