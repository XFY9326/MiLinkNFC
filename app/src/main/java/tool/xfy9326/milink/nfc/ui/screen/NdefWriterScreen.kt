package tool.xfy9326.milink.nfc.ui.screen

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.vm.NfcWriterViewModel
import tool.xfy9326.milink.nfc.utils.showToast

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        NdefWriterScreen(
            ndefWriteData = NdefWriteData(
                msg = NdefMessage(NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)),
                readOnly = false
            ),
            onNavBack = {},
        )
    }
}

@Composable
fun NdefWriterScreen(
    viewModel: NfcWriterViewModel = viewModel(),
    ndefWriteData: NdefWriteData,
    onNavBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                onNavBack = onNavBack,
                onRequestExportNdefBin = viewModel::requestExportNdefBin
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .displayCutoutPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    modifier = Modifier
                        .size(62.dp),
                    imageVector = Icons.Default.Nfc,
                    contentDescription = stringResource(id = R.string.nfc_writer),
                    tint = if (ndefWriteData.readOnly) Color.Red else LocalContentColor.current
                )
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = stringResource(
                        id = if (ndefWriteData.readOnly) {
                            R.string.tap_and_write_nfc_tag_read_only
                        } else {
                            R.string.tap_and_write_nfc_tag
                        }
                    ),
                    textAlign = TextAlign.Center,
                    style = typography.bodyLarge
                )
                ndefWriteData.msg.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            id = R.string.nfc_write_bytes,
                            it.byteArrayLength
                        ),
                        textAlign = TextAlign.Center,
                        style = typography.bodyMedium
                    )
                }
            }
        }
    }
    EventHandler(
        viewModel = viewModel
    )
}

@Composable
private fun TopBar(
    onNavBack: () -> Unit,
    onRequestExportNdefBin: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.nfc_write_ndef)) },
        navigationIcon = {
            IconButton(onClick = onNavBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.nav_back)
                )
            }
        },
        actions = {
            IconButton(onClick = onRequestExportNdefBin) {
                Icon(
                    imageVector = Icons.Outlined.SaveAlt,
                    contentDescription = stringResource(id = R.string.export)
                )
            }
        }
    )
}

@Composable
private fun EventHandler(viewModel: NfcWriterViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.instantMsg.collectLatest {
            context.showToast(it.resId)
        }
    }
}
