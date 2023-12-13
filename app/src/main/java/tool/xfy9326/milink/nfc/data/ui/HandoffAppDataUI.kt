package tool.xfy9326.milink.nfc.data.ui

import lib.xfy9326.xiaomi.nfc.HandoffAppData
import tool.xfy9326.milink.nfc.utils.toHexString
import tool.xfy9326.milink.nfc.utils.toHexText

class HandoffAppDataUI(
    val majorVersion: String,
    val minorVersion: String,
    val deviceType: String,
    val attributesMap: Map<String, String>,
    val action: String,
    val payloadsMap: Map<String, String>
) {
    constructor(handoffAppData: HandoffAppData) : this(
        majorVersion = handoffAppData.majorVersion.toHexString(true),
        minorVersion = handoffAppData.minorVersion.toHexString(true),
        deviceType = handoffAppData.deviceType.name,
        attributesMap = handoffAppData.attributesMap.map { it.key.toHexString(true) to it.value.toHexText() }.toMap(),
        action = handoffAppData.action,
        payloadsMap = handoffAppData.payloadsMap.map {
            it.key.name to if (it.key.isText == true) {
                it.value.toHexText()
            } else {
                it.value.toString(Charsets.UTF_8)
            }
        }.toMap(),
    )
}