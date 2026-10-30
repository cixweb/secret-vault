package io.github.cixweb.secretvault.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.cixweb.secretvault.data.FileInspector
import io.github.cixweb.secretvault.data.FileReport
import io.github.cixweb.secretvault.data.MigrationOutcome
import io.github.cixweb.secretvault.data.SecretStore
import io.github.cixweb.secretvault.data.SecretVault
import io.github.cixweb.secretvault.data.Verdict
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Everything the screen shows.
 *
 * @property input the text in the field, not saved yet.
 * @property stored the secret as last read from or written to the store.
 * @property revealed whether the secret is shown instead of masked.
 * @property migration what the migration did at start.
 * @property plaintextPresent whether a legacy file waits for the next start to migrate it.
 * @property wasReset whether the store replaced a file that did not decrypt.
 * @property unavailable whether the vault failed to open or to save; the field, Save and
 *   Clear are disabled.
 * @property report what the encrypted file on disk holds.
 */
data class SecretUiState(
    val input: String = "",
    val stored: String = "",
    val revealed: Boolean = false,
    val migration: MigrationOutcome = MigrationOutcome.NotRun,
    val plaintextPresent: Boolean = false,
    val wasReset: Boolean = false,
    val unavailable: Boolean = false,
    val report: FileReport = FileReport(SecretStore.ENCRYPTED_FILE_NAME, 0, "", Verdict.NoSecret),
)

/**
 * Holds the screen state and runs every action on the vault.
 *
 * @param openVault opens the vault in the given scope. It is called once and must not
 *   block, as [SecretVault.open] does by moving to `Dispatchers.IO`. Tests pass one with
 *   an in-memory key.
 */
class SecretViewModel(openVault: suspend (CoroutineScope) -> SecretVault) : ViewModel() {
    // Opened off the main thread; every action waits for it.
    private val vault: Deferred<SecretVault> = viewModelScope.async { openVault(viewModelScope) }

    private val _state = MutableStateFlow(SecretUiState())
    /** The state to draw; it changes after every action. */
    val state: StateFlow<SecretUiState> = _state.asStateFlow()

    init {
        withVault { vault ->
            // The first read opens the store, runs the migration and any corruption reset.
            val value = vault.store.secret.first()
            _state.update {
                it.copy(
                    input = value,
                    migration = vault.migration.outcome,
                    wasReset = vault.corruption.happened,
                )
            }
            refresh(vault, value)
        }
    }

    /** Updates the field; nothing is saved until [save]. */
    fun onInputChange(value: String) {
        _state.update { it.copy(input = value) }
    }

    /** Shows or masks the secret, in the field and in the stored value. */
    fun onRevealChange(revealed: Boolean) {
        _state.update { it.copy(revealed = revealed) }
    }

    /** Saves the text of the field as the secret, then inspects the file again. */
    fun save() = withVault { vault ->
        val value = _state.value.input
        vault.store.save(value)
        refresh(vault, value)
    }

    /** Stores an empty secret and empties the field. */
    fun clear() = withVault { vault ->
        vault.store.clear()
        _state.update { it.copy(input = "") }
        refresh(vault, "")
    }

    /** Writes a plaintext file as an older version would have; the next start migrates it. */
    fun seedLegacyFile() = withVault { vault ->
        withContext(Dispatchers.IO) { vault.plaintextFile.writeText(LEGACY_SAMPLE) }
        refresh(vault, _state.value.stored)
    }

    /**
     * Runs [block] once the vault is open. A Keystore or disk failure becomes a state the
     * screen shows, never a crash.
     */
    private fun withVault(block: suspend (SecretVault) -> Unit) {
        viewModelScope.launch {
            try {
                block(vault.await())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(unavailable = true) }
            }
        }
    }

    private suspend fun refresh(vault: SecretVault, stored: String) {
        val (report, plaintext) = withContext(Dispatchers.IO) {
            FileInspector.inspect(vault.encryptedFile, stored) to vault.plaintextFile.exists()
        }
        _state.update { it.copy(stored = stored, report = report, plaintextPresent = plaintext) }
    }

    private companion object {
        const val LEGACY_SAMPLE = "legacy-secret-1234"
    }
}
