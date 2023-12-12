package tool.xfy9326.milink.nfc.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NfcActionIntentType
import tool.xfy9326.milink.nfc.data.ScreenMirror
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.utils.EMPTY

@Preview(showBackground = true)
@Composable
private fun Preview() {
    var screenMirror by rememberSaveable {
        mutableStateOf(
            ScreenMirror(
                deviceType = ScreenMirror.DeviceType.PC,
                actionIntentType = NfcActionIntentType.FAKE_NFC_TAG,
                bluetoothMac = EMPTY,
                enableLyra = true
            )
        )
    }

    AppTheme {
        MirrorDataController(
            screenMirror = screenMirror,
            onChanged = { screenMirror = it },
        )
    }
}

@Composable
fun MirrorDataController(
    modifier: Modifier = Modifier,
    screenMirror: ScreenMirror,
    onChanged: (ScreenMirror) -> Unit
) {
    Column(modifier = modifier) {
        MiConnectActionSettings(
            modifier = Modifier.fillMaxWidth(),
            deviceType = screenMirror.deviceType,
            actionIntentType = screenMirror.actionIntentType,
            onDeviceTypeChanged = { onChanged(screenMirror.copy(deviceType = it)) },
            onMirrorTypeChanged = { onChanged(screenMirror.copy(actionIntentType = it)) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        MacAddressTextField(
            modifier = Modifier.fillMaxWidth(),
            value = screenMirror.bluetoothMac,
            upperCase = true,
            onValueChange = { onChanged(screenMirror.copy(bluetoothMac = it)) }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = screenMirror.enableLyra,
                onCheckedChange = { onChanged(screenMirror.copy(enableLyra = it)) }
            )
            Text(text = stringResource(id = R.string.enable_lyra_ability))
        }
    }
}