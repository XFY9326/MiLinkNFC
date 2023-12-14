package tool.xfy9326.milink.nfc.data

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import lib.xfy9326.xiaomi.nfc.NfcTagDeviceRecord
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc
import tool.xfy9326.milink.nfc.utils.hexToByteArray

@Parcelize
data class Circulate(
    val deviceType: DeviceType,
    val actionIntentType: NfcActionIntentType,
    val wifiMac: String,
    val bluetoothMac: String,
) : Parcelable {
    enum class DeviceType(val nfcTagDeviceType: NfcTagDeviceRecord.DeviceType, @StringRes val resId: Int) {
        ROUTER(NfcTagDeviceRecord.DeviceType.MI_ROUTER, R.string.device_type_router),
        SOUND_BOX(NfcTagDeviceRecord.DeviceType.MI_SOUND_BOX, R.string.device_type_sound_box),
        LAPTOP(NfcTagDeviceRecord.DeviceType.MI_LAPTOP, R.string.device_type_laptop),
        TV(NfcTagDeviceRecord.DeviceType.MI_TV, R.string.device_type_tv),
        PHONE(NfcTagDeviceRecord.DeviceType.MI_PHONE, R.string.device_type_phone),
    }

    fun toConfig(writeTime: Int) =
        XiaomiNfc.Circulate.Config(
            writeTime = writeTime,
            deviceType = deviceType.nfcTagDeviceType,
            wifiMac = wifiMac.hexToByteArray(true),
            bluetoothMac = bluetoothMac.hexToByteArray(true)
        )
}