package io.github.cixweb.secretvault.crypto

import com.google.crypto.tink.Aead

/** Source of the [Aead] that protects the stored secret. */
interface AeadProvider {
    /**
     * The [Aead] that encrypts and decrypts the secret file. It may read the keyset from
     * disk and ask the Android Keystore to decrypt it: call it off the main thread.
     *
     * @throws java.security.GeneralSecurityException when the key cannot be created or used.
     * @throws java.io.IOException when the keyset cannot be read or written.
     */
    fun aead(): Aead
}
