package io.github.cixweb.secretvault.data

import io.github.cixweb.secretvault.crypto.InMemoryAeadProvider
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PlaintextMigrationTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val aeadProvider = InMemoryAeadProvider()

    /** Opens the vault, reads once, then closes it as an app restart would. */
    private fun openAndRead(): Pair<SecretVault, String> = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val vault = SecretVault.open(folder.root, aeadProvider, scope)
            vault to vault.store.secret.first()
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun migratesExistingPlaintextAndDeletesTheFile() {
        val legacy = File(folder.root, SecretStore.PLAINTEXT_FILE_NAME)
        legacy.writeText("old-secret")

        val (vault, value) = openAndRead()

        assertEquals("old-secret", value)
        assertEquals(MigrationOutcome.Migrated, vault.migration.outcome)
        assertFalse(legacy.exists())
        assertTrue(vault.encryptedFile.exists())
        assertFalse(FileInspector.containsSecret(vault.encryptedFile.readBytes(), "old-secret"))
    }

    @Test
    fun withoutOldDataNothingHappens() {
        val (vault, value) = openAndRead()

        assertEquals("", value)
        assertEquals(MigrationOutcome.NothingToMigrate, vault.migration.outcome)
        assertFalse(vault.plaintextFile.exists())
    }

    @Test
    fun secondRunDoesNothing() {
        File(folder.root, SecretStore.PLAINTEXT_FILE_NAME).writeText("old-secret")
        openAndRead()

        val (vault, value) = openAndRead()

        assertEquals("old-secret", value)
        assertEquals(MigrationOutcome.NothingToMigrate, vault.migration.outcome)
        assertFalse(vault.plaintextFile.exists())
    }
}
