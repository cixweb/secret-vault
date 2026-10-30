package io.github.cixweb.secretvault.data

import java.io.File

/** What the bytes of a file say about the secret. */
enum class Verdict {
    /** There is no secret yet, or no file. */
    NoSecret,

    /** The file holds data, and the secret is not among its bytes. */
    Encrypted,

    /** The secret is in the file as plain UTF-8. */
    Exposed,
}

/**
 * What [FileInspector] found in one file.
 *
 * @property fileName the name of the file, without its directory.
 * @property sizeBytes the size on disk; 0 when the file does not exist.
 * @property hexPreview the first bytes in hex, as the screen shows them.
 * @property verdict whether the secret can be read from the bytes.
 */
data class FileReport(
    val fileName: String,
    val sizeBytes: Long,
    val hexPreview: String,
    val verdict: Verdict,
)

/** Reads a file as raw bytes and checks whether the secret appears in them. */
object FileInspector {
    private const val PREVIEW_BYTES = 24

    /**
     * Reads [file] and looks for [secret] in its bytes. Blocking I/O: call it off the main
     * thread.
     *
     * @param file the file to read; a missing file counts as empty.
     * @param secret the value the store holds; empty means there is nothing to look for.
     * @return the size of the file, its first 24 bytes in hex and the verdict.
     */
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

    /**
     * Whether the UTF-8 bytes of [secret] appear in [bytes] at any offset. Finding them
     * proves the secret is exposed; not finding them does not prove the encryption is
     * strong.
     */
    fun containsSecret(bytes: ByteArray, secret: String): Boolean {
        val needle = secret.toByteArray(Charsets.UTF_8)
        if (needle.isEmpty() || needle.size > bytes.size) return false
        return (0..bytes.size - needle.size).any { start ->
            needle.indices.all { bytes[start + it] == needle[it] }
        }
    }
}
