package tool.xfy9326.milink.nfc.data.ui

import tool.xfy9326.milink.nfc.data.NdefReadData

class NfcTagInfoUI(
    val techList: List<String>,
    val type: String,
    val currentSize: Int,
    val maxSize: Int,
    val writeable: Boolean,
    val canMakeReadOnly: Boolean
) {
    constructor(ndefReadData: NdefReadData) : this(
        techList = ndefReadData.techList,
        type = ndefReadData.type,
        currentSize = ndefReadData.msg.byteArrayLength,
        maxSize = ndefReadData.maxSize,
        writeable = ndefReadData.writeable,
        canMakeReadOnly = ndefReadData.canMakeReadOnly,
    )
}