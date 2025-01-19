package tool.xfy9326.milink.nfc.data.nfc

import androidx.annotation.StringRes
import lib.xfy9326.xiaomi.nfc.HandoffAppData
import lib.xfy9326.xiaomi.nfc.NfcTagActionRecord
import lib.xfy9326.xiaomi.nfc.NfcTagAppData
import lib.xfy9326.xiaomi.nfc.NfcTagDeviceRecord
import lib.xfy9326.xiaomi.nfc.XiaomiNdefType
import lib.xfy9326.xiaomi.nfc.XiaomiNfcPayload
import lib.xfy9326.xiaomi.nfc.XiaomiNfcProtocol
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.utils.toHexString
import tool.xfy9326.milink.nfc.utils.toHexText
import java.text.SimpleDateFormat

data class XiaomiNdefData(
    val ndefType: XiaomiNdefType,
    val payload: Payload,
    val appData: App,
) : NdefData {
    companion object {
        fun parse(ndefType: XiaomiNdefType, payload: XiaomiNfcPayload<*>): XiaomiNdefData =
            XiaomiNdefData(
                ndefType = ndefType,
                payload = Payload(payload),
                appData = when (val appData = payload.appData) {
                    is HandoffAppData -> App.Handoff(appData)
                    is NfcTagAppData -> App.NfcTag(appData, ndefType)
                }
            )
    }

    data class Payload(
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

    sealed interface TagRecord {
        data class Action(
            val action: String,
            val condition: String,
            val deviceNumber: String,
            val flags: String,
            val conditionParameters: String?
        ) : TagRecord {
            constructor(actionRecord: NfcTagActionRecord) : this(
                action = actionRecord.enumAction.name,
                condition = actionRecord.enumCondition.name,
                deviceNumber = actionRecord.deviceNumber.toHexString(true),
                flags = actionRecord.flags.toHexString(true),
                conditionParameters = actionRecord.conditionParameters?.toHexString(true),
            )
        }

        data class Device(
            val deviceType: String,
            val flags: String,
            val deviceNumber: String,
            val attributesMap: Map<String, String>,
        ) : TagRecord {
            constructor(
                deviceRecord: NfcTagDeviceRecord,
                action: NfcTagActionRecord.Action,
                ndefPayloadType: XiaomiNdefType
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
                                val text =
                                    it.value.runCatching {
                                        toString(Charsets.UTF_8)
                                    }.onFailure {
                                        it.printStackTrace()
                                    }.getOrNull()
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

    }

    sealed interface App {
        data class Handoff(
            val majorVersion: String,
            val minorVersion: String,
            val deviceType: String,
            val attributesMap: Map<String, String>,
            val action: String,
            val payloadsMap: Map<String, String>
        ) : App {
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

        data class NfcTag(
            val majorVersion: String,
            val minorVersion: String,
            val writeTime: String,
            val flags: String,
            val actionRecord: TagRecord.Action?,
            val deviceRecord: TagRecord.Device?,
        ) : App {
            constructor(nfcTagAppData: NfcTagAppData, ndefPayloadType: XiaomiNdefType) : this(
                majorVersion = nfcTagAppData.majorVersion.toHexString(true),
                minorVersion = nfcTagAppData.minorVersion.toHexString(true),
                writeTime = SimpleDateFormat.getDateTimeInstance()
                    .format(nfcTagAppData.writeTime * 1000L),
                flags = nfcTagAppData.flags.toHexString(true),
                actionRecord = nfcTagAppData.firstOrNullActionRecord()
                    ?.let { TagRecord.Action(it) },
                deviceRecord = nfcTagAppData.firstOrNullDeviceRecord()
                    ?.let {
                        TagRecord.Device(
                            it,
                            nfcTagAppData.firstEnumAction(),
                            ndefPayloadType
                        )
                    }
            )
        }
    }
}