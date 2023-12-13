package tool.xfy9326.milink.nfc.data.ui

import lib.xfy9326.xiaomi.nfc.XiaomiNfcPayload
import tool.xfy9326.milink.nfc.utils.EMPTY
import tool.xfy9326.milink.nfc.utils.toHexString

class XiaomiNfcPayloadUI(
    val majorVersion: String,
    val minorVersion: String,
    val idHash: String?,
    val protocol: String,
) {
    constructor(xiaomiNfcPayload: XiaomiNfcPayload<*>) : this(
        majorVersion = xiaomiNfcPayload.majorVersion.toString(),
        minorVersion = xiaomiNfcPayload.minorVersion.toString(),
        idHash = xiaomiNfcPayload.idHash?.toHexString(true),
        protocol = xiaomiNfcPayload.protocol::class.simpleName ?: EMPTY
    )
}