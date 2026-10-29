package io.github.cixweb.secretvault.data

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler

/**
 * What happens when the encrypted file cannot be decrypted: the Keystore key is gone, the file
 * came from a backup of another device, or it was tampered with. Instead of failing every read,
 * DataStore replaces the file with an empty secret, and [happened] lets the UI say so.
 */
class CorruptionReset {
    @Volatile
    var happened: Boolean = false
        private set

    val handler = ReplaceFileCorruptionHandler<String> {
        happened = true
        SecretSerializer.defaultValue
    }
}
