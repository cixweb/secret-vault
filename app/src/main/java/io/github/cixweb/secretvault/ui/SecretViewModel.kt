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

class SecretViewModel(openVault: suspend (CoroutineScope) -> SecretVault) : ViewModel() {
    // Opened off the main thread; every action waits for it.
    private val vault: Deferred<SecretVault> = viewModelScope.async { openVault(viewModelScope) }

    private val _state = MutableStateFlow(SecretUiState())
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

    fun onInputChange(value: String) {
        _state.update { it.copy(input = value) }
    }

    fun onRevealChange(revealed: Boolean) {
        _state.update { it.copy(revealed = revealed) }
    }

    fun save() = withVault { vault ->
        val value = _state.value.input
        vault.store.save(value)
        refresh(vault, value)
    }

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
