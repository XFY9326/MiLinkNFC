package tool.xfy9326.milink.nfc.data.ui

import androidx.annotation.StringRes
import lib.xfy9326.xiaomi.nfc.XiaomiNfcPayload
import lib.xfy9326.xiaomi.nfc.XiaomiNfcProtocol
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.utils.toHexString

class XiaomiNfcPayloadUI(
    val majorVersion: String,
    val minorVersion: String,
    val idHash: String?,
    val protocol: Protocol,
) {
    constructor(xiaomiNfcPayload: XiaomiNfcPayload<*>) : this(
        majorVersion = xiaomiNfcPayload.majorVersion.toString(),
        minorVersion = xiaomiNfcPayload.minorVersion.toString(),
        idHash = xiaomiNfcPayload.idHash?.toHexString(true),
        protocol = when (xiaomiNfcPayload.protocol) {
            XiaomiNfcProtocol.HandOff -> Protocol.HAND_OFF
            XiaomiNfcProtocol.V1 -> Protocol.V1
            XiaomiNfcProtocol.V2 -> Protocol.V2
        }
    )

    enum class Protocol(@StringRes val resId: Int) {
        V1(R.string.protocol_v1),
        V2(R.string.protocol_v2),
        HAND_OFF(R.string.protocol_handoff);
    }
}
