package tool.xfy9326.milink.nfc.ui.dialog

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.ui.common.DialogContentSurface
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeTypography

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        NdefWriterDialog(
            ndefData = NdefWriteData(
                ndefMsg = NdefMessage(NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)),
                readOnly = false
            ),
            onOpenReader = {},
            onCloseReader = {},
            onDismissRequest = {}
        )
    }
}

@Composable
fun NdefWriterDialog(
    ndefData: NdefWriteData,
    onOpenReader: (NdefWriteData) -> Unit,
    onCloseReader: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val typography = LocalAppThemeTypography.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onOpenReader(ndefData)
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                onCloseReader()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            onCloseReader()
        }
    }

    AlertDialog(onDismissRequest = onDismissRequest) {
        DialogContentSurface {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.padding(46.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier
                            .size(48.dp),
                        imageVector = Icons.Default.Nfc,
                        contentDescription = stringResource(id = R.string.write_nfc),
                        tint = if (ndefData.readOnly) Color.Red else LocalContentColor.current
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        text = stringResource(
                            id = if (ndefData.readOnly) {
                                R.string.put_and_write_nfc_tag_read_only
                            } else {
                                R.string.put_and_write_nfc_tag
                            }
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.nfc_write_bytes, ndefData.ndefMsg.byteArrayLength),
                        textAlign = TextAlign.Center,
                        style = typography.labelMedium
                    )
                }
            }
        }
    }
}