package tool.xfy9326.milink.nfc.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeColorScheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeTypography

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        InfoContent(
            title = "TITLE",
            data = listOf(
                "Title 1" to "Content",
                "Title 2" to "Content",
                "Title 3" to "Content: Content\nContent: Content\nContent: Content",
                "Title 4" to "Content",
                "Title 5" to "Content",
            )
        )
    }
}

@Composable
fun InfoContent(
    modifier: Modifier = Modifier,
    title: String? = null,
    data: List<Pair<String, String>>,
) {
    val typography = LocalAppThemeTypography.current
    val colorScheme = LocalAppThemeColorScheme.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        if (title != null) {
            Text(text = title, style = typography.titleLarge)
        }
        for ((key, value) in data) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = key, style = typography.titleMedium)
                Text(text = value, color = colorScheme.secondary, style = typography.bodySmall)
            }
        }
    }
}