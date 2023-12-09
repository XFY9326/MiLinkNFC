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
import tool.xfy9326.milink.nfc.data.XiaomiDeviceType
import tool.xfy9326.milink.nfc.data.XiaomiMirrorType
import tool.xfy9326.milink.nfc.ui.theme.AppTheme

@Preview(showBackground = true)
@Composable
private fun Preview() {
    var deviceType by remember { mutableStateOf(XiaomiDeviceType.PC) }
    var mirrorType by remember { mutableStateOf(XiaomiMirrorType.FAKE_NFC_TAG) }

    AppTheme {
        MiConnectActionSettings(
            deviceType = deviceType,
            mirrorType = mirrorType,
            onDeviceTypeChanged = { deviceType = it },
            onMirrorTypeChanged = { mirrorType = it }
        )
    }
}

@Composable
fun MiConnectActionSettings(
    modifier: Modifier = Modifier,
    deviceType: XiaomiDeviceType,
    mirrorType: XiaomiMirrorType,
    onDeviceTypeChanged: (XiaomiDeviceType) -> Unit,
    onMirrorTypeChanged: (XiaomiMirrorType) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SelectorTextField(
            modifier = Modifier.weight(0.4f),
            label = stringResource(id = R.string.nfc_xiaomi_device_type),
            selectKey = deviceType.name,
            keyTextMap = XiaomiDeviceType.entries.associate { it.name to stringResource(id = it.resId) },
            onKeySelected = {
                onDeviceTypeChanged(XiaomiDeviceType.valueOf(it))
            }
        )
        SelectorTextField(
            modifier = Modifier.weight(0.6f),
            label = stringResource(id = R.string.nfc_mirror_intent_type),
            selectKey = mirrorType.name,
            keyTextMap = XiaomiMirrorType.entries.associate { it.name to stringResource(id = it.resId) },
            onKeySelected = {
                onMirrorTypeChanged(XiaomiMirrorType.valueOf(it))
            }
        )
    }
}

