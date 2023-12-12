package tool.xfy9326.milink.nfc.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeColorScheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeTypography

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        EntryCard(
            icon = Icons.Default.Android,
            title = "Entry",
            summary = "Summary text",
            onClick = {}
        )
    }
}

@Composable
fun EntryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    summary: String,
    innerPadding: PaddingValues = PaddingValues(20.dp),
    onClick: () -> Unit,
) {
    val colorScheme = LocalAppThemeColorScheme.current
    val typography = LocalAppThemeTypography.current

    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier
                    .size(40.dp)
                    .background(color = colorScheme.onSurfaceVariant, shape = CircleShape)
                    .padding(8.dp),
                tint = colorScheme.inverseOnSurface
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = title, style = typography.titleMedium)
                Text(text = summary, style = typography.bodySmall)
            }
        }
    }
}