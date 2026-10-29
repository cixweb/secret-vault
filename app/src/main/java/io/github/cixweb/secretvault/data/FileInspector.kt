package io.github.cixweb.secretvault.data

import java.io.File

enum class Verdict { NoSecret, Encrypted, Exposed }

data class FileReport(
    val fileName: String,
    val sizeBytes: Long,
    val hexPreview: String,
    val verdict: Verdict,
)

/** Reads a file as raw bytes and checks whether the secret appears in them. */
object FileInspector {
    private const val PREVIEW_BYTES = 24

    fun inspect(file: File, secret: String): FileReport {
        val bytes = if (file.exists()) file.readBytes() else ByteArray(0)
        val verdict = when {
            secret.isEmpty() || bytes.isEmpty() -> Verdict.NoSecret
            containsSecret(bytes, secret) -> Verdict.Exposed
            else -> Verdict.Encrypted
        }
        return FileReport(
            fileName = file.name,
            sizeBytes = bytes.size.toLong(),
            hexPreview = bytes.take(PREVIEW_BYTES).joinToString(" ") { "%02x".format(it) },
            verdict = verdict,
        )
    }

    fun containsSecret(bytes: ByteArray, secret: String): Boolean {
        val needle = secret.toByteArray(Charsets.UTF_8)
        if (needle.isEmpty() || needle.size > bytes.size) return false
        return (0..bytes.size - needle.size).any { start ->
            needle.indices.all { bytes[start + it] == needle[it] }
        }
    }
}
