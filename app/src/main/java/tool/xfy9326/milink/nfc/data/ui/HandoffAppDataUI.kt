package tool.xfy9326.milink.nfc.data.ui

import lib.xfy9326.xiaomi.nfc.HandoffAppData
import lib.xfy9326.xiaomi.nfc.NfcTagAppData
import lib.xfy9326.xiaomi.nfc.XiaomiNdefTNF
import tool.xfy9326.milink.nfc.utils.toHexString
import tool.xfy9326.milink.nfc.utils.toHexText
import java.text.SimpleDateFormat

sealed interface AppDataUI

class HandoffAppDataUI(
    val majorVersion: String,
    val minorVersion: String,
    val deviceType: String,
    val attributesMap: Map<String, String>,
    val action: String,
    val payloadsMap: Map<String, String>
) : AppDataUI {
    constructor(handoffAppData: HandoffAppData) : this(
        majorVersion = handoffAppData.majorVersion.toHexString(true),
        minorVersion = handoffAppData.minorVersion.toHexString(true),
        deviceType = handoffAppData.enumDeviceType.name,
        attributesMap = handoffAppData.attributesMap.map { it.key.toHexString(true) to it.value.toHexText() }
            .toMap(),
        action = handoffAppData.action,
        payloadsMap = handoffAppData.enumPayloadsMap.map {
            it.key.name to if (it.key.isText == true) {
                it.value.toString(Charsets.UTF_8)
            } else {
                it.value.toHexText()
            }
        }.toMap(),
    )
}

class NfcTagAppDataUI(
    val majorVersion: String,
    val minorVersion: String,
    val writeTime: String,
    val flags: String,
    val actionRecord: NfcTagActionRecordUI?,
    val deviceRecord: NfcTagDeviceRecordUI?,
) : AppDataUI {
    constructor(nfcTagAppData: NfcTagAppData, ndefPayloadType: XiaomiNdefTNF) : this(
        majorVersion = nfcTagAppData.majorVersion.toHexString(true),
        minorVersion = nfcTagAppData.minorVersion.toHexString(true),
        writeTime = SimpleDateFormat.getDateTimeInstance().format(nfcTagAppData.writeTime * 1000L),
        flags = nfcTagAppData.flags.toHexString(true),
        actionRecord = nfcTagAppData.firstOrNullActionRecord()?.let { NfcTagActionRecordUI(it) },
        deviceRecord = nfcTagAppData.firstOrNullDeviceRecord()
            ?.let { NfcTagDeviceRecordUI(it, nfcTagAppData.firstEnumAction(), ndefPayloadType) }
    )
}