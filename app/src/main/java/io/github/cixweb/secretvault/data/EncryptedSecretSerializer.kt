package io.github.cixweb.secretvault.data

import androidx.datastore.core.Serializer
import androidx.datastore.tink.AeadSerializer
import com.google.crypto.tink.Aead

/**
 * Encrypts what [SecretSerializer] writes. The file name is the associated data, so a file
 * copied or renamed to another name no longer decrypts; failures surface as CorruptionException.
 */
fun encryptedSecretSerializer(aead: Aead, fileName: String): Serializer<String> =
    AeadSerializer(
        aead = aead,
        wrappedSerializer = SecretSerializer,
        associatedData = fileName.toByteArray(Charsets.UTF_8),
    )
