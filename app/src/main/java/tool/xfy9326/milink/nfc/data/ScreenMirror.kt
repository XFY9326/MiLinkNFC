package tool.xfy9326.milink.nfc.data

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import lib.xfy9326.xiaomi.nfc.HandoffAppData
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc

@Parcelize
data class ScreenMirror(
    val deviceType: DeviceType,
    val actionIntentType: NfcActionIntentType,
    val bluetoothMac: String,
    val enableLyra: Boolean
) : Parcelable {
    enum class DeviceType(val handOffType: HandoffAppData.DeviceType, @StringRes val resId: Int) {
        TV(HandoffAppData.DeviceType.TV, R.string.device_type_tv),
        PC(HandoffAppData.DeviceType.PC, R.string.device_type_pc),
        CAR(HandoffAppData.DeviceType.CAR, R.string.device_type_car),
        PAD(HandoffAppData.DeviceType.PAD, R.string.device_type_pad);
    }

    fun toConfig() =
        XiaomiNfc.ScreenMirror.Config(
            deviceType = deviceType.handOffType,
            bluetoothMac = bluetoothMac,
            enableLyra = enableLyra
        )
}

