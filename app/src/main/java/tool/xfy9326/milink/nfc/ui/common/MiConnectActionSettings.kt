package tool.xfy9326.milink.nfc.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NfcActionIntentType
import tool.xfy9326.milink.nfc.data.ScreenMirror
import tool.xfy9326.milink.nfc.ui.theme.AppTheme

@Preview(showBackground = true)
@Composable
private fun Preview() {
    var deviceType by remember { mutableStateOf(ScreenMirror.DeviceType.PC) }
    var mirrorType by remember { mutableStateOf(NfcActionIntentType.FAKE_NFC_TAG) }

    AppTheme {
        MiConnectActionSettings(
            deviceType = deviceType,
            actionIntentType = mirrorType,
            onDeviceTypeChanged = { deviceType = it },
            onMirrorTypeChanged = { mirrorType = it }
        )
    }
}

@Composable
fun MiConnectActionSettings(
    modifier: Modifier = Modifier,
    deviceType: ScreenMirror.DeviceType,
    actionIntentType: NfcActionIntentType,
    onDeviceTypeChanged: (ScreenMirror.DeviceType) -> Unit,
    onMirrorTypeChanged: (NfcActionIntentType) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SelectorTextField(
            modifier = Modifier.weight(0.4f),
            label = stringResource(id = R.string.handoff_device_type),
            selectKey = deviceType.name,
            keyTextMap = ScreenMirror.DeviceType.entries.associate { it.name to stringResource(id = it.resId) },
            onKeySelected = {
                onDeviceTypeChanged(ScreenMirror.DeviceType.valueOf(it))
            }
        )
        SelectorTextField(
            modifier = Modifier.weight(0.6f),
            label = stringResource(id = R.string.nfc_action_intent),
            selectKey = actionIntentType.name,
            keyTextMap = NfcActionIntentType.entries.associate { it.name to stringResource(id = it.resId) },
            onKeySelected = {
                onMirrorTypeChanged(NfcActionIntentType.valueOf(it))
            }
        )
    }
}

