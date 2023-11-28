package tool.xfy9326.milink.nfc.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class XiaomiNFCTagData(
    val deviceType: XiaomiDeviceType,
    val btMac: String,
    val readOnly: Boolean
) : Parcelable
