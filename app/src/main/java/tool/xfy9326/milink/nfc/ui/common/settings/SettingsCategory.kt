package tool.xfy9326.milink.nfc.ui.common.settings

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tool.xfy9326.milink.nfc.data.settings.SettingsGroup
import tool.xfy9326.milink.nfc.data.settings.SettingsItem
import tool.xfy9326.milink.nfc.ui.common.update
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeColorScheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeTypography

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        SettingsCategory(
            group = SettingsGroup(
                title = "Title",
                items = listOf(
                    SettingsItem.Switch(
                        title = "Switch 1",
                        summary = "Summary text 1",
                        checked = false,
                        onValueChanged = {}
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SettingsCategory(
    group: SettingsGroup,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val colorScheme = LocalAppThemeColorScheme.current
    val typography = LocalAppThemeTypography.current

    LazyColumn(modifier = modifier) {
        item {
            Text(
                modifier = Modifier
                    .padding(innerPadding.update(bottom = 0.dp))
                    .padding(bottom = 6.dp),
                color = colorScheme.tertiary,
                text = group.title,
                style = typography.titleSmall
            )
        }
        for (settingsItem in group.items) {
            item {
                SettingsItemScopeImpl(innerPadding = innerPadding.update(vertical = 0.dp)).apply {
                    when (settingsItem) {
                        is SettingsItem.Switch -> SettingsItemSwitch(settingsItem)
                        is SettingsItem.Content -> SettingsItemContent(settingsItem)
                    }
                }
            }
        }
        innerPadding.calculateBottomPadding().let {
            if (it.value > 0) {
                item {
                    Spacer(modifier = Modifier.height(it))
                }
            }
        }
    }
}

@LayoutScopeMarker
@Immutable
interface SettingsItemScope {
    @Stable
    fun Modifier.innerPadding(): Modifier
}

class SettingsItemScopeImpl(private val innerPadding: PaddingValues) : SettingsItemScope {
    override fun Modifier.innerPadding(): Modifier =
        this.padding(innerPadding)
}
