package tool.xfy9326.milink.nfc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppThemeColorScheme = staticCompositionLocalOf<ColorScheme> {
    error("CompositionLocal LocalAppThemeColorScheme not present")
}
val LocalAppThemeShapes = staticCompositionLocalOf<Shapes> {
    error("CompositionLocal LocalAppThemeShapes not present")
}
val LocalAppThemeTypography = staticCompositionLocalOf<Typography> {
    error("CompositionLocal LocalAppThemeTypography not present")
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = colorScheme(darkTheme, dynamicColor)
    val shapes = MaterialTheme.shapes
    val typography = MaterialTheme.typography
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        typography = typography
    ) {
        CompositionLocalProvider(
            LocalAppThemeColorScheme provides colorScheme,
            LocalAppThemeShapes provides shapes,
            LocalAppThemeTypography provides typography,
            content = content
        )
    }
}