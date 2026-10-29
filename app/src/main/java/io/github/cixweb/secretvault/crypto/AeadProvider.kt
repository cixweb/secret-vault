package io.github.cixweb.secretvault.crypto

import com.google.crypto.tink.Aead

/** Source of the [Aead] that protects the stored secret. */
interface AeadProvider {
    fun aead(): Aead
}
