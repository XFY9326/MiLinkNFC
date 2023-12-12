package tool.xfy9326.milink.nfc.data

import tool.xfy9326.milink.nfc.protocol.XiaomiNfc

data class HuaweiRedirect(
    val deviceType: ScreenMirror.DeviceType,
    val actionIntentType: NfcActionIntentType,
    val enableLyra: Boolean
) {
    fun toConfig(bluetoothMac: String) =
        XiaomiNfc.ScreenMirror.Config(
            deviceType = deviceType.handOffType,
            bluetoothMac = bluetoothMac,
            enableLyra = enableLyra
        )
}
