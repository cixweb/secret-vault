package io.github.cixweb.secretvault.data

import androidx.datastore.core.CorruptionException
import io.github.cixweb.secretvault.crypto.InMemoryAeadProvider
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class EncryptedSecretSerializerTest {
    private val aead = InMemoryAeadProvider().aead()

    private fun encrypt(value: String, fileName: String = "secret.enc"): ByteArray {
        val out = ByteArrayOutputStream()
        runBlocking { encryptedSecretSerializer(aead, fileName).writeTo(value, out) }
        return out.toByteArray()
    }

    @Test
    fun roundTrips() {
        val bytes = encrypt("round-trip")
        val value = runBlocking {
            encryptedSecretSerializer(aead, "secret.enc").readFrom(ByteArrayInputStream(bytes))
        }
        assertEquals("round-trip", value)
    }

    @Test
    fun differentAssociatedDataIsCorruption() {
        val bytes = encrypt("bound", fileName = "secret.enc")
        assertThrows(CorruptionException::class.java) {
            runBlocking {
                encryptedSecretSerializer(aead, "other.enc").readFrom(ByteArrayInputStream(bytes))
            }
        }
    }

    @Test
    fun differentKeyIsCorruption() {
        val bytes = encrypt("keyed")
        val otherAead = InMemoryAeadProvider().aead()
        assertThrows(CorruptionException::class.java) {
            runBlocking {
                encryptedSecretSerializer(otherAead, "secret.enc")
                    .readFrom(ByteArrayInputStream(bytes))
            }
        }
    }
}
