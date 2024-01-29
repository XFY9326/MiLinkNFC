package tool.xfy9326.milink.nfc.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
        FunctionCard(
            icon = Icons.Default.Android,
            title = "Function",
            extraIconContent = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.HelpOutline,
                        contentDescription = null
                    )
                }
            },
            description = "Description line 1\n\nDescription line 2\n\nDescription line 3"
        ) {
            Text(text = "Content")
        }
    }
}

@Composable
fun FunctionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    extraIconContent: (@Composable RowScope.() -> Unit)? = null,
    description: String? = null,
    innerPadding: PaddingValues = PaddingValues(18.dp),
    onClick: () -> Unit = {},
    content: (@Composable () -> Unit)? = null
) {
    val colorScheme = LocalAppThemeColorScheme.current
    val typography = LocalAppThemeTypography.current

    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier
                        .size(38.dp)
                        .background(color = colorScheme.onSurfaceVariant, shape = CircleShape)
                        .padding(8.dp),
                    tint = colorScheme.inverseOnSurface
                )
                Text(text = title, style = typography.titleLarge)
                extraIconContent?.let {
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        content = it
                    )
                }
            }
            if (description != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = description,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    style = typography.bodyMedium
                )
            }
            if (content != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    content()
                }
            }
        }
    }
}
