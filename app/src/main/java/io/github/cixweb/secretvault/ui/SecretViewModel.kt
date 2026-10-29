package io.github.cixweb.secretvault.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.cixweb.secretvault.data.FileInspector
import io.github.cixweb.secretvault.data.FileReport
import io.github.cixweb.secretvault.data.MigrationOutcome
import io.github.cixweb.secretvault.data.SecretStore
import io.github.cixweb.secretvault.data.SecretVault
import io.github.cixweb.secretvault.data.Verdict
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    val migration: MigrationOutcome = MigrationOutcome.NotRun,
    val plaintextPresent: Boolean = false,
    val report: FileReport = FileReport(SecretStore.ENCRYPTED_FILE_NAME, 0, "", Verdict.NoSecret),
)

class SecretViewModel(openVault: (CoroutineScope) -> SecretVault) : ViewModel() {
    private val vault: SecretVault = openVault(viewModelScope)

    private val _state = MutableStateFlow(SecretUiState())
    val state: StateFlow<SecretUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // The first read opens the store and runs the migration.
            val value = vault.store.secret.first()
            _state.update { it.copy(input = value, migration = vault.migration.outcome) }
            refresh(value)
        }
    }

    fun onInputChange(value: String) {
        _state.update { it.copy(input = value) }
    }

    fun save() {
        viewModelScope.launch {
            val value = _state.value.input
            vault.store.save(value)
            refresh(value)
        }
    }

    fun clear() {
        viewModelScope.launch {
            vault.store.clear()
            _state.update { it.copy(input = "") }
            refresh("")
        }
    }

    /** Writes a plaintext file as an older version would have; the next start migrates it. */
    fun seedLegacyFile() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { vault.plaintextFile.writeText(LEGACY_SAMPLE) }
            refresh(_state.value.stored)
        }
    }

    private suspend fun refresh(stored: String) {
        val (report, plaintext) = withContext(Dispatchers.IO) {
            FileInspector.inspect(vault.encryptedFile, stored) to vault.plaintextFile.exists()
        }
        _state.update { it.copy(stored = stored, report = report, plaintextPresent = plaintext) }
    }

    private companion object {
        const val LEGACY_SAMPLE = "legacy-secret-1234"
    }
}
