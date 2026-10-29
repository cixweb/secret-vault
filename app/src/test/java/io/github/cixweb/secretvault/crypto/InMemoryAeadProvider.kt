package io.github.cixweb.secretvault.crypto

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig

/** A real Tink AES256_GCM keyset held in memory, for JVM tests where there is no Keystore. */
class InMemoryAeadProvider : AeadProvider {
    private val aead: Aead

    init {
        AeadConfig.register()
        val handle = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
        aead = handle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    override fun aead(): Aead = aead
}
