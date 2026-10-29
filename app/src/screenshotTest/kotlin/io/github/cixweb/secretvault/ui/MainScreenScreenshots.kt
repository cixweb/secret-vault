package io.github.cixweb.secretvault.ui

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.cixweb.secretvault.ui.components.ScreenPreviews
import io.github.cixweb.secretvault.ui.theme.AppTheme

@PreviewTest
@ScreenPreviews
@Composable
fun MainScreenScreenshot() {
    AppTheme {
        MainScreen()
    }
}
