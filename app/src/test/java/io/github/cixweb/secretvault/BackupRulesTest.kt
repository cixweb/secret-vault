package io.github.cixweb.secretvault

import io.github.cixweb.secretvault.crypto.KeystoreAeadProvider
import io.github.cixweb.secretvault.data.SecretStore
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test

/** The vault files and the keyset stay out of every backup the rules describe. */
class BackupRulesTest {
    private val expected = setOf(
        "file:${SecretStore.ENCRYPTED_FILE_NAME}",
        "file:${SecretStore.PLAINTEXT_FILE_NAME}",
        "sharedpref:${KeystoreAeadProvider.KEYSET_PREFS}.xml",
    )

    private fun excludes(file: String, section: String): Set<String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(File("src/main/res/xml/$file"))
        val parent = document.getElementsByTagName(section).item(0)
        val nodes = parent.childNodes
        return (0 until nodes.length)
            .map { nodes.item(it) }
            .filter { it.nodeName == "exclude" }
            .map {
                val domain = it.attributes.getNamedItem("domain").nodeValue
                "$domain:${it.attributes.getNamedItem("path").nodeValue}"
            }
            .toSet()
    }

    @Test
    fun cloudBackupLeavesTheVaultOut() =
        assertEquals(expected, excludes("data_extraction_rules.xml", "cloud-backup"))

    @Test
    fun deviceTransferLeavesTheVaultOut() =
        assertEquals(expected, excludes("data_extraction_rules.xml", "device-transfer"))

    @Test
    fun fullBackupLeavesTheVaultOut() =
        assertEquals(expected, excludes("backup_rules.xml", "full-backup-content"))
}
