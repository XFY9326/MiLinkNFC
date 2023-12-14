package tool.xfy9326.milink.nfc.data.settings

import androidx.compose.ui.graphics.vector.ImageVector

sealed interface SettingsItem<T> {
    val title: String
    val summary: String?
    val icon: ImageVector?
    val onValueChanged: (T) -> Unit

    class Content(
        override val title: String,
        override val summary: String? = null,
        override val icon: ImageVector? = null,
        override val onValueChanged: (Unit) -> Unit
    ) : SettingsItem<Unit>

    class Switch(
        override val title: String,
        override val summary: String? = null,
        override val icon: ImageVector? = null,
        override val onValueChanged: (Boolean) -> Unit,
        val checked: Boolean,
    ) : SettingsItem<Boolean>
}