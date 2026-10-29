package io.github.cixweb.secretvault.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.cixweb.secretvault.R
import io.github.cixweb.secretvault.ui.components.AppScaffold
import io.github.cixweb.secretvault.ui.components.ScreenPreviews
import io.github.cixweb.secretvault.ui.components.SectionCard
import io.github.cixweb.secretvault.ui.theme.AppTheme

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    AppScaffold(
        title = stringResource(R.string.app_name),
        subtitle = stringResource(R.string.app_tagline),
        modifier = modifier,
    ) {
        SectionCard(title = stringResource(R.string.main_section_title)) {
            Text(
                text = stringResource(R.string.main_section_body),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@ScreenPreviews
@Composable
fun MainScreenPreview() {
    AppTheme {
        MainScreen()
    }
}
