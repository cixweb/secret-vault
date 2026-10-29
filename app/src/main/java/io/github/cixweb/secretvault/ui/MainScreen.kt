package io.github.cixweb.secretvault.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import io.github.cixweb.secretvault.ui.theme.AppTheme

@Composable
fun MainScreen(viewModel: SecretViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsState()
    MainContent(
        state = state,
        onInputChange = viewModel::onInputChange,
        onSave = viewModel::save,
        onClear = viewModel::clear,
        onSeedLegacy = viewModel::seedLegacyFile,
        modifier = modifier,
    )
}

@Composable
fun MainContent(
    state: SecretUiState,
    onInputChange: (String) -> Unit,
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
        SectionCard(title = stringResource(R.string.secret_section_title)) {
            AppTextField(
                value = state.input,
                onValueChange = onInputChange,
                label = stringResource(R.string.secret_label),
            )
            PrimaryButton(text = stringResource(R.string.action_save), onClick = onSave)
            SecondaryButton(text = stringResource(R.string.action_clear), onClick = onClear)
            LabeledValue(
                label = stringResource(R.string.stored_label),
                value = state.stored.ifEmpty { stringResource(R.string.stored_empty) },
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
        MainContent(previewState, {}, {}, {}, {})
    }
}
