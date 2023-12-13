package tool.xfy9326.milink.nfc.data.ui

import lib.xfy9326.xiaomi.nfc.XiaomiNdefPayloadType
import tool.xfy9326.milink.nfc.data.NdefReadData

class XiaomiNfcTagUI(
    val ndefPayloadType: XiaomiNdefPayloadType,
    val techList: List<String>,
    val type: String,
    val currentSize: Int,
    val maxSize: Int,
    val writeable: Boolean,
    val canMakeReadOnly: Boolean
) {
    constructor(type: XiaomiNdefPayloadType, ndefReadData: NdefReadData) : this(
        ndefPayloadType = type,
        techList = ndefReadData.techList,
        type = ndefReadData.type,
        currentSize = ndefReadData.msg.byteArrayLength,
        maxSize = ndefReadData.maxSize,
        writeable = ndefReadData.writeable,
        canMakeReadOnly = ndefReadData.canMakeReadOnly,
    )
}