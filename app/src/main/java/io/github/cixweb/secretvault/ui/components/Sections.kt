package io.github.cixweb.secretvault.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.cixweb.secretvault.R
import io.github.cixweb.secretvault.ui.theme.AppTheme

/**
 * A titled block of content on the screen background: the unit every screen is made of.
 *
 * @param modifier applied to the card, which fills the width.
 * @param title read as a heading by screen readers; null for a card without one.
 * @param content the rows of the card, 12 dp apart.
 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() },
                )
            }
            content()
        }
    }
}

/**
 * One label and its value on a row, read together by screen readers.
 *
 * @param label what the value is, on the left.
 * @param value the value, on the right; it wraps when long.
 * @param modifier applied to the row.
 * @param monospace true for file names, keys and numbers.
 */
@Composable
fun LabeledValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    monospace: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = if (monospace) AppTheme.typography.code else MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Code, bytes or log lines, in monospace. Long lines scroll sideways instead of
 * wrapping, so the columns of a hex dump stay aligned.
 *
 * @param text the lines to show, as they are.
 * @param modifier applied to the block, which fills the width.
 */
@Composable
fun CodeBlock(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Text(
            text = text,
            style = AppTheme.typography.code,
            softWrap = false,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
        )
    }
}

/** The kind of a [StatusBanner], which sets its colors and icon. */
enum class Status { Success, Warning, Error, Info }

/**
 * The outcome of an action or a check, announced to screen readers when it changes.
 *
 * @param text one short sentence.
 * @param status the kind of outcome, which sets the colors and the icon.
 * @param modifier applied to the banner, which fills the width.
 */
@Composable
fun StatusBanner(
    text: String,
    status: Status,
    modifier: Modifier = Modifier,
) {
    val (container, content) = statusColors(status)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(statusIcon(status)),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(text = text, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun statusColors(status: Status): Pair<Color, Color> = when (status) {
    Status.Success -> AppTheme.colors.successContainer to AppTheme.colors.onSuccessContainer
    Status.Warning -> AppTheme.colors.warningContainer to AppTheme.colors.onWarningContainer
    Status.Error -> MaterialTheme.colorScheme.errorContainer to
        MaterialTheme.colorScheme.onErrorContainer
    Status.Info -> MaterialTheme.colorScheme.primaryContainer to
        MaterialTheme.colorScheme.onPrimaryContainer
}

@DrawableRes
private fun statusIcon(status: Status): Int = when (status) {
    Status.Success -> R.drawable.ic_status_success
    Status.Warning -> R.drawable.ic_status_warning
    Status.Error -> R.drawable.ic_status_error
    Status.Info -> R.drawable.ic_status_info
}
