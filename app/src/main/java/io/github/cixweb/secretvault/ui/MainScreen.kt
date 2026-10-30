package io.github.cixweb.secretvault.ui

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import io.github.cixweb.secretvault.R
import io.github.cixweb.secretvault.data.FileReport
import io.github.cixweb.secretvault.data.MigrationOutcome
import io.github.cixweb.secretvault.data.Verdict
import io.github.cixweb.secretvault.ui.components.AppScaffold
import io.github.cixweb.secretvault.ui.components.AppTextField
import io.github.cixweb.secretvault.ui.components.CodeBlock
import io.github.cixweb.secretvault.ui.components.LabeledValue
import io.github.cixweb.secretvault.ui.components.PrimaryButton
import io.github.cixweb.secretvault.ui.components.ScreenPreviews
import io.github.cixweb.secretvault.ui.components.SecondaryButton
import io.github.cixweb.secretvault.ui.components.SectionCard
import io.github.cixweb.secretvault.ui.components.Status
import io.github.cixweb.secretvault.ui.components.StatusBanner
import io.github.cixweb.secretvault.ui.components.SwitchRow
import io.github.cixweb.secretvault.ui.theme.AppTheme

/**
 * The vault screen, connected to [viewModel].
 *
 * @param viewModel holds the state and runs the actions.
 * @param modifier applied to the whole screen.
 */
@Composable
fun MainScreen(viewModel: SecretViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsState()
    MainContent(
        state = state,
        onInputChange = viewModel::onInputChange,
        onRevealChange = viewModel::onRevealChange,
        onSave = viewModel::save,
        onClear = viewModel::clear,
        onSeedLegacy = viewModel::seedLegacyFile,
        modifier = modifier,
    )
}

/**
 * The vault screen drawn from [state] alone, so previews and screenshot tests need no
 * ViewModel.
 *
 * @param state what to show.
 * @param onInputChange called with the new text on every edit.
 * @param onRevealChange called when the Show secret switch changes.
 * @param onSave called by Save.
 * @param onClear called by Clear.
 * @param onSeedLegacy called by Create legacy plaintext file.
 * @param modifier applied to the whole screen.
 */
@Composable
fun MainContent(
    state: SecretUiState,
    onInputChange: (String) -> Unit,
    onRevealChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onSeedLegacy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(R.string.app_name),
        subtitle = stringResource(R.string.app_tagline),
        modifier = modifier,
    ) {
        if (state.unavailable) {
            StatusBanner(text = stringResource(R.string.vault_unavailable), status = Status.Error)
        }
        SectionCard(title = stringResource(R.string.secret_section_title)) {
            if (state.wasReset) {
                StatusBanner(text = stringResource(R.string.vault_reset), status = Status.Warning)
            }
            AppTextField(
                value = state.input,
                onValueChange = onInputChange,
                label = stringResource(R.string.secret_label),
                enabled = !state.unavailable,
                // Hidden by default, and kept out of the keyboard's suggestions and learning.
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    autoCorrectEnabled = false,
                ),
                visualTransformation = if (state.revealed) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
            )
            SwitchRow(
                label = stringResource(R.string.reveal_label),
                checked = state.revealed,
                onCheckedChange = onRevealChange,
            )
            PrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = onSave,
                enabled = !state.unavailable,
            )
            SecondaryButton(
                text = stringResource(R.string.action_clear),
                onClick = onClear,
                enabled = !state.unavailable,
            )
            LabeledValue(
                label = stringResource(R.string.stored_label),
                value = when {
                    state.stored.isEmpty() -> stringResource(R.string.stored_empty)
                    state.revealed -> state.stored
                    else -> MASK
                },
            )
        }
        SectionCard(title = stringResource(R.string.migration_section_title)) {
            when {
                state.plaintextPresent -> StatusBanner(
                    text = stringResource(R.string.migration_pending),
                    status = Status.Warning,
                )
                state.migration == MigrationOutcome.Migrated -> StatusBanner(
                    text = stringResource(R.string.migration_done),
                    status = Status.Success,
                )
                else -> StatusBanner(
                    text = stringResource(R.string.migration_none),
                    status = Status.Info,
                )
            }
            SecondaryButton(
                text = stringResource(R.string.action_seed_legacy),
                onClick = onSeedLegacy,
            )
        }
        SectionCard(title = stringResource(R.string.disk_section_title)) {
            val (text, status) = when (state.report.verdict) {
                Verdict.NoSecret -> stringResource(R.string.disk_empty) to Status.Info
                Verdict.Encrypted -> stringResource(R.string.disk_encrypted) to Status.Success
                Verdict.Exposed -> stringResource(R.string.disk_exposed) to Status.Error
            }
            StatusBanner(text = text, status = status)
            LabeledValue(
                label = stringResource(R.string.disk_file),
                value = state.report.fileName,
                monospace = true,
            )
            LabeledValue(
                label = stringResource(R.string.disk_size),
                value = stringResource(R.string.disk_size_value, state.report.sizeBytes),
                monospace = true,
            )
            if (state.report.hexPreview.isNotEmpty()) {
                CodeBlock(text = state.report.hexPreview)
            }
        }
    }
}

private const val MASK = "••••••••"

/** An encrypted secret on disk: the base state of the previews and screenshot tests. */
val previewState = SecretUiState(
    input = "hunter2-token",
    stored = "hunter2-token",
    migration = MigrationOutcome.NothingToMigrate,
    report = FileReport(
        fileName = "secret.enc",
        sizeBytes = 49,
        hexPreview = "0a 1f 7c d2 91 3b e0 44 58 a6 0c 9d 12 f3 b8 6e 21 05 c7 4a 83 de 19 70",
        verdict = Verdict.Encrypted,
    ),
)

@ScreenPreviews
@Composable
fun MainScreenPreview() {
    AppTheme {
        MainContent(previewState, {}, {}, {}, {}, {})
    }
}
