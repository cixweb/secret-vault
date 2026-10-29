package io.github.cixweb.secretvault.ui

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.cixweb.secretvault.data.FileReport
import io.github.cixweb.secretvault.data.MigrationOutcome
import io.github.cixweb.secretvault.data.Verdict
import io.github.cixweb.secretvault.ui.components.ScreenPreviews
import io.github.cixweb.secretvault.ui.theme.AppTheme

@Composable
private fun Screen(state: SecretUiState) {
    AppTheme {
        MainContent(state, {}, {}, {}, {})
    }
}

@PreviewTest
@ScreenPreviews
@Composable
fun MainScreenScreenshot() {
    Screen(SecretUiState())
}

@PreviewTest
@ScreenPreviews
@Composable
fun EncryptedStoredScreenshot() {
    Screen(previewState)
}

@PreviewTest
@ScreenPreviews
@Composable
fun MigratedScreenshot() {
    Screen(previewState.copy(migration = MigrationOutcome.Migrated))
}

@PreviewTest
@ScreenPreviews
@Composable
fun LegacyFilePendingScreenshot() {
    Screen(
        previewState.copy(
            plaintextPresent = true,
            report = FileReport("secret.enc", 0, "", Verdict.NoSecret),
        ),
    )
}
