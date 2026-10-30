package io.github.cixweb.secretvault.data

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler

/**
 * What happens when the encrypted file cannot be decrypted: the Keystore key is gone, the file
 * came from a backup of another device, or it was tampered with. Instead of failing every read,
 * DataStore replaces the file with an empty secret, and [happened] lets the UI say so.
 */
class CorruptionReset {
    /** True once [handler] has replaced a file that did not decrypt in this process. */
    @Volatile
    var happened: Boolean = false
        private set

    /** Give it to the DataStore: it records the reset and starts from an empty secret. */
    val handler = ReplaceFileCorruptionHandler<String> {
        happened = true
        SecretSerializer.defaultValue
    }
}
