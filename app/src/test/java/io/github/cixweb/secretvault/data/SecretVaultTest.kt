package io.github.cixweb.secretvault.data

import com.google.crypto.tink.Aead
import io.github.cixweb.secretvault.crypto.AeadProvider
import io.github.cixweb.secretvault.crypto.InMemoryAeadProvider
import java.io.File
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SecretVaultTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @After
    fun tearDown() = scope.cancel()

    private fun open(provider: AeadProvider): SecretVault = runBlocking {
        SecretVault.open(folder.root, provider, scope)
    }

    @Test
    fun theAeadIsBuiltOnTheIoDispatcher() {
        val io = Executors.newSingleThreadExecutor { Thread(it, "vault-io") }
        var thread = ""
        val recording = object : AeadProvider {
            private val real = InMemoryAeadProvider()

            override fun aead(): Aead {
                thread = Thread.currentThread().name
                return real.aead()
            }
        }
        try {
            runBlocking {
                SecretVault.open(folder.root, recording, scope, io.asCoroutineDispatcher())
            }
        } finally {
            io.shutdown()
        }
        // Debug builds of coroutines append " @coroutine#N" to the thread name.
        assertTrue(thread, thread.startsWith("vault-io"))
    }

    @Test
    fun aFileThatDoesNotDecryptIsResetInsteadOfFailing() {
        File(folder.root, SecretStore.ENCRYPTED_FILE_NAME).writeBytes(byteArrayOf(1, 2, 3, 4))
        val vault = open(InMemoryAeadProvider())

        val value = runBlocking { vault.store.secret.first() }

        assertEquals("", value)
        assertTrue(vault.corruption.happened)
    }

    @Test
    fun aLostKeyResetsTheSecretAndTheStoreKeepsWorking() {
        // First run, then the process ends: one DataStore per file at a time.
        val firstRun = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        runBlocking { SecretVault.open(folder.root, InMemoryAeadProvider(), firstRun).store.save("old") }
        firstRun.cancel()

        // A new keyset stands for a restored file whose Keystore key is gone.
        val vault = open(InMemoryAeadProvider())

        assertEquals("", runBlocking { vault.store.secret.first() })
        assertTrue(vault.corruption.happened)
        runBlocking { vault.store.save("new") }
        assertEquals("new", runBlocking { vault.store.secret.first() })
    }

    @Test
    fun aReadableFileIsNotReset() {
        val vault = open(InMemoryAeadProvider())
        runBlocking { vault.store.save("kept") }

        assertEquals("kept", runBlocking { vault.store.secret.first() })
        assertFalse(vault.corruption.happened)
    }
}
