package io.github.cixweb.secretvault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/** Rounded corners, from 8 dp on small controls to 28 dp on sheets. */
internal val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val LocalAppColors = staticCompositionLocalOf { LightAppColors }
private val LocalAppTypography = staticCompositionLocalOf { AppTypography() }

/**
 * The app theme: Signal colors, Inter and JetBrains Mono, rounded shapes. Dynamic color
 * is off on purpose, so the app looks the same on every device and in every screenshot.
 *
 * @param darkTheme whether to use the dark colors; follows the system by default.
 * @param content the screen, which reads the theme through MaterialTheme and [AppTheme].
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppColors provides if (darkTheme) DarkAppColors else LightAppColors,
        LocalAppTypography provides AppTypography(),
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypographyM3,
            shapes = AppShapes,
            content = content,
        )
    }
}

/** Theme values beyond Material 3: status colors and monospace styles. */
object AppTheme {
    /** Status colors of the current theme, light or dark. */
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    /** Monospace styles for code, keys and bytes. */
    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current
}
