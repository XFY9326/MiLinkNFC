package tool.xfy9326.milink.nfc.ui.dialog

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.ui.theme.AppTheme

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        NotSupportedOSDialog(
            onConfirmed = {},
            onExit = {}
        )
    }
}

@Composable
fun NotSupportedOSDialog(
    onConfirmed: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onExit,
        properties = DialogProperties(dismissOnClickOutside = false),
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = stringResource(id = R.string.not_supported_os),
                tint = Color.Red
            )
        },
        title = {
            Text(text = stringResource(id = R.string.not_supported_os))
        },
        text = {
            Text(text = stringResource(id = R.string.not_supported_os_desc))
        },
        confirmButton = {
            Button(onClick = onConfirmed) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(modifier = Modifier.padding(horizontal = 8.dp), onClick = onExit) {
                Text(text = stringResource(id = R.string.exit))
            }
        }
    )
}