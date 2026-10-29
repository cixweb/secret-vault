package io.github.cixweb.secretvault.data

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

/** Stores the secret as its raw UTF-8 bytes. Used as is for the legacy file and wrapped for the new one. */
object SecretSerializer : Serializer<String> {
    override val defaultValue: String = ""

    override suspend fun readFrom(input: InputStream): String =
        input.readBytes().toString(Charsets.UTF_8)

    override suspend fun writeTo(t: String, output: OutputStream) {
        output.write(t.toByteArray(Charsets.UTF_8))
    }
}
