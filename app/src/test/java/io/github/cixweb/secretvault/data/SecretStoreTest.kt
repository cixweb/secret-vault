package io.github.cixweb.secretvault.data

import io.github.cixweb.secretvault.crypto.InMemoryAeadProvider
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

class SecretStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun plaintextStoreWritesAndReads() = runBlocking {
        val store = SecretStore.plaintext(File(folder.root, "secret.txt"), scope)
        assertEquals("", store.secret.first())

        store.save("hello")

        assertEquals("hello", store.secret.first())
    }

    @Test
    fun encryptedStoreWritesAndReads() = runBlocking {
        val store = SecretStore.encrypted(
            File(folder.root, "secret.enc"),
            InMemoryAeadProvider().aead(),
            scope,
        )
        store.save("hello")
        assertEquals("hello", store.secret.first())

        store.clear()
        assertEquals("", store.secret.first())
    }

    @Test
    fun plaintextFileHoldsTheSecretButEncryptedFileDoesNot() = runBlocking {
        val secret = "known-secret-value-42"
        val legacy = File(folder.root, "secret.txt")
        val encrypted = File(folder.root, "secret.enc")

        SecretStore.plaintext(legacy, scope).save(secret)
        SecretStore.encrypted(encrypted, InMemoryAeadProvider().aead(), scope).save(secret)

        assertTrue(FileInspector.containsSecret(legacy.readBytes(), secret))
        assertFalse(FileInspector.containsSecret(encrypted.readBytes(), secret))
        assertEquals(Verdict.Exposed, FileInspector.inspect(legacy, secret).verdict)
        assertEquals(Verdict.Encrypted, FileInspector.inspect(encrypted, secret).verdict)
    }
}
