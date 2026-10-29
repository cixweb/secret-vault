package io.github.cixweb.secretvault.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class AeadProviderTest {
    private val plain = "top secret".toByteArray()
    private val context = "secret.enc".toByteArray()

    @Test
    fun encryptsAndDecryptsBytes() {
        val aead = InMemoryAeadProvider().aead()
        val cipher = aead.encrypt(plain, context)

        assertFalse(cipher.contentEquals(plain))
        assertArrayEquals(plain, aead.decrypt(cipher, context))
    }

    @Test
    fun anotherKeyCannotDecrypt() {
        val cipher = InMemoryAeadProvider().aead().encrypt(plain, context)

        assertThrows(java.security.GeneralSecurityException::class.java) {
            InMemoryAeadProvider().aead().decrypt(cipher, context)
        }
    }
}
