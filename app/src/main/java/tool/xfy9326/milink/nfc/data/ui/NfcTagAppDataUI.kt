package tool.xfy9326.milink.nfc.data.ui

import lib.xfy9326.xiaomi.nfc.NfcTagAppData
import lib.xfy9326.xiaomi.nfc.XiaomiNdefPayloadType
import tool.xfy9326.milink.nfc.utils.toHexString
import java.text.SimpleDateFormat

class NfcTagAppDataUI(
    val majorVersion: String,
    val minorVersion: String,
    val writeTime: String,
    val flags: String,
    val actionRecord: NfcTagActionRecordUI?,
    val deviceRecord: NfcTagDeviceRecordUI?,
) {
    constructor(nfcTagAppData: NfcTagAppData, ndefPayloadType: XiaomiNdefPayloadType) : this(
        majorVersion = nfcTagAppData.majorVersion.toHexString(true),
        minorVersion = nfcTagAppData.minorVersion.toHexString(true),
        writeTime = SimpleDateFormat.getDateTimeInstance().format(nfcTagAppData.writeTime * 1000L),
        flags = nfcTagAppData.flags.toHexString(true),
        actionRecord = nfcTagAppData.getActionRecord()?.let { NfcTagActionRecordUI(it) },
        deviceRecord = nfcTagAppData.getDeviceRecord()?.let { NfcTagDeviceRecordUI(it, nfcTagAppData.getActionRecord(), ndefPayloadType) }
    )
}