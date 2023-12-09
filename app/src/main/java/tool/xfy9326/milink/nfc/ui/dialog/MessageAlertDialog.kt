package tool.xfy9326.milink.nfc.ui.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import tool.xfy9326.milink.nfc.ui.theme.AppTheme

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        MessageAlertDialog(
            title = "Title",
            message = "Message",
            onConfirm = {},
            onDismissRequest = {},
            icon = Icons.Default.Warning,
            iconTint = Color.Red,
            showCancel = true
        )
    }
}

@Composable
fun MessageAlertDialog(
    title: String,
    message: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = LocalContentColor.current,
    showCancel: Boolean = false,
    addMessagePrefixSpaces: Boolean = false,
    highLightConfirmButton: Boolean = false,
    highLightCancelButton: Boolean = false,
    confirmText: String = stringResource(id = android.R.string.ok),
    cancelText: String = stringResource(id = android.R.string.cancel),
    onConfirm: () -> Unit = onDismissRequest,
    onCancel: () -> Unit = onDismissRequest,
    properties: DialogProperties = DialogProperties()
) {
    val locale = Locale.current

    val scrollState = rememberScrollState()
    var showMessage by remember { mutableStateOf(message) }

    AlertDialog(
        modifier = modifier.fillMaxWidth(),
        onDismissRequest = onDismissRequest,
        properties = properties,
        icon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = title,
                    tint = iconTint
                )
            }
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(
                modifier = Modifier.verticalScroll(scrollState),
                text = showMessage, onTextLayout = if (addMessagePrefixSpaces) {
                    {
                        showMessage = if (locale.language == java.util.Locale.CHINA.language && it.lineCount > 1) {
                            "\u3000\u3000" + message
                        } else {
                            message
                        }
                    }
                } else {
                    {}
                }
            )
        },
        confirmButton = {
            if (highLightConfirmButton) {
                Button(onClick = onConfirm) {
                    Text(text = confirmText)
                }
            } else {
                TextButton(onClick = onConfirm) {
                    Text(text = confirmText)
                }
            }
        },
        dismissButton = if (showCancel) {
            {
                if (highLightCancelButton) {
                    Button(onClick = onCancel) {
                        Text(text = cancelText)
                    }
                } else {
                    TextButton(onClick = onCancel) {
                        Text(text = cancelText)
                    }
                }
            }
        } else null
    )
}