package tool.xfy9326.milink.nfc.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tool.xfy9326.milink.nfc.data.XiaomiDeviceType
import tool.xfy9326.milink.nfc.data.XiaomiMirrorData
import tool.xfy9326.milink.nfc.data.XiaomiMirrorType
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.utils.EMPTY

@Preview(showBackground = true)
@Composable
private fun Preview() {
    var mirrorData by rememberSaveable {
        mutableStateOf(
            XiaomiMirrorData(
                XiaomiDeviceType.PC,
                XiaomiMirrorType.FAKE_NFC_TAG,
                EMPTY
            )
        )
    }

    AppTheme {
        MirrorDataController(
            mirrorData = mirrorData,
            onChanged = { mirrorData = it },
        )
    }
}

@Composable
fun MirrorDataController(
    modifier: Modifier = Modifier,
    mirrorData: XiaomiMirrorData,
    onChanged: (XiaomiMirrorData) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MiConnectActionSettings(
            modifier = Modifier.fillMaxWidth(),
            deviceType = mirrorData.deviceType,
            mirrorType = mirrorData.mirrorType,
            onDeviceTypeChanged = { onChanged(mirrorData.copy(deviceType = it)) },
            onMirrorTypeChanged = { onChanged(mirrorData.copy(mirrorType = it)) }
        )
        MacAddressTextField(
            modifier = Modifier.fillMaxWidth(),
            value = mirrorData.btMac,
            onValueChange = { onChanged(mirrorData.copy(btMac = it)) }
        )
    }
}