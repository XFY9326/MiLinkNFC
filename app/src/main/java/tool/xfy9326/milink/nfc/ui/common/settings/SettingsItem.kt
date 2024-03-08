package tool.xfy9326.milink.nfc.ui.common.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tool.xfy9326.milink.nfc.data.settings.SettingsItem
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeColorScheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeTypography

@Preview(showBackground = true)
@Composable
private fun PreviewSettingsItem() {
    AppTheme {
        SettingsItemScopeImpl(PaddingValues(0.dp)).apply {
            SettingsItemContent(
                item = SettingsItem.Content(
                    title = "Title",
                    summary = "Summary text",
                    icon = Icons.Default.Settings,
                    onValueChanged = {}
                )
            )
        }
    }
}

@Composable
private fun SettingsItemScope.SettingsItem(
    item: SettingsItem<*>,
    suffixContent: (@Composable () -> Unit)? = null,
    onContextClickListener: () -> Unit
) {
    val colorScheme = LocalAppThemeColorScheme.current
    val typography = LocalAppThemeTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onContextClickListener() }
            .padding(vertical = 12.dp)
            .innerPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        AnimatedVisibility(visible = item.icon != null) {
            item.icon?.let {
                Icon(
                    modifier = Modifier.size(26.dp),
                    imageVector = it,
                    tint = colorScheme.surfaceTint,
                    contentDescription = item.title
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(text = item.title, style = typography.bodyLarge)
            AnimatedVisibility(visible = item.summary != null) {
                item.summary?.let {
                    Text(text = it, color = colorScheme.secondary, style = typography.bodyMedium)
                }
            }
        }
        AnimatedVisibility(visible = suffixContent != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VerticalDivider(
                    modifier = Modifier.height(24.dp)
                )
                suffixContent?.invoke()
            }
        }
    }
}

@Composable
fun SettingsItemScope.SettingsItemContent(item: SettingsItem.Content) {
    SettingsItem(
        item = item,
        onContextClickListener = {
            item.onValueChanged(Unit)
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewSettingsItemSwitch() {
    AppTheme {
        SettingsItemScopeImpl(PaddingValues(0.dp)).apply {
            SettingsItemSwitch(
                item = SettingsItem.Switch(
                    title = "Title",
                    summary = "Summary text",
                    icon = Icons.Default.Settings,
                    checked = false,
                    onValueChanged = {}
                )
            )
        }
    }
}

@Composable
fun SettingsItemScope.SettingsItemSwitch(item: SettingsItem.Switch) {
    SettingsItem(
        item = item,
        suffixContent = {
            Switch(checked = item.checked, onCheckedChange = item.onValueChanged)
        },
        onContextClickListener = {
            item.onValueChanged(!item.checked)
        }
    )
}