package tool.xfy9326.milink.nfc.data.ui

import lib.xfy9326.xiaomi.nfc.NfcTagActionRecord
import lib.xfy9326.xiaomi.nfc.NfcTagDeviceRecord
import lib.xfy9326.xiaomi.nfc.XiaomiNdefTNF
import tool.xfy9326.milink.nfc.utils.toHexString
import tool.xfy9326.milink.nfc.utils.toHexText

sealed interface NfcTagRecordUI

class NfcTagActionRecordUI(
    val action: String,
    val condition: String,
    val deviceNumber: String,
    val flags: String,
    val conditionParameters: String?
) : NfcTagRecordUI {
    constructor(actionRecord: NfcTagActionRecord) : this(
        action = actionRecord.enumAction.name,
        condition = actionRecord.enumCondition.name,
        deviceNumber = actionRecord.deviceNumber.toHexString(true),
        flags = actionRecord.flags.toHexString(true),
        conditionParameters = actionRecord.conditionParameters?.toHexString(true),
    )
}

class NfcTagDeviceRecordUI(
    val deviceType: String,
    val flags: String,
    val deviceNumber: String,
    val attributesMap: Map<String, String>,
) : NfcTagRecordUI {
    constructor(
        deviceRecord: NfcTagDeviceRecord,
        action: NfcTagActionRecord.Action,
        ndefPayloadType: XiaomiNdefTNF
    ) : this(
        deviceType = deviceRecord.enumDeviceType.name,
        flags = deviceRecord.flags.toHexString(true),
        deviceNumber = deviceRecord.deviceNumber.toHexString(true),
        attributesMap = if (action == NfcTagActionRecord.Action.UNKNOWN) {
            deviceRecord.attributesMap.map { it.key.toString() to it.value.toHexText() }
        } else {
            deviceRecord.getAllAttributesMap(action, ndefPayloadType).mapNotNull {
                if (it.value.isNotEmpty()) {
                    val key = it.key.attributeName
                    val value = if (
                        it.key == NfcTagDeviceRecord.DeviceAttribute.APP_DATA &&
                        NfcTagDeviceRecord.getAppDataValueType(
                            bytes = it.value,
                            action = action,
                            ndefType = ndefPayloadType
                        ) != NfcTagDeviceRecord.AppDataValueType.IOT_ACTION
                    ) {
                        it.value.toHexText()
                    } else {
                        val text = it.value.runCatching { toString(Charsets.UTF_8) }.getOrNull()
                        val hexText = it.value.toHexText()
                        when (it.key.isText) {
                            true -> text ?: hexText
                            false -> hexText
                            else -> "$text\n$hexText"
                        }
                    }
                    key to value
                } else {
                    null
                }
            }
        }.toMap()
    )
}