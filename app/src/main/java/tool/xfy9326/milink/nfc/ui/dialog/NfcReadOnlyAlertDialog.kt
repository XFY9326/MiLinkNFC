package tool.xfy9326.milink.nfc.ui.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.ui.theme.AppTheme

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        NfcReadOnlyAlertDialog(
            onConfirm = {},
            onDismissRequest = {}
        )
    }
}

@Composable
fun NfcReadOnlyAlertDialog(
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    MessageAlertDialog(
        title = stringResource(id = R.string.dangerous_action_alert),
        message = stringResource(id = R.string.set_nfc_read_only_desc),
        icon = Icons.Default.Warning,
        iconTint = Color.Red,
        onConfirm = onConfirm,
        addMessagePrefixSpaces = true,
        showCancel = true,
        onDismissRequest = onDismissRequest
    )
}