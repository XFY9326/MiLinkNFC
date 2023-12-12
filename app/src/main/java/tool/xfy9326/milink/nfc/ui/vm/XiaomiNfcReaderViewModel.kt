package tool.xfy9326.milink.nfc.ui.vm

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.xfy9326.xiaomi.nfc.HandoffAppData
import lib.xfy9326.xiaomi.nfc.NfcTagActionRecord
import lib.xfy9326.xiaomi.nfc.NfcTagAppData
import lib.xfy9326.xiaomi.nfc.NfcTagDeviceRecord
import lib.xfy9326.xiaomi.nfc.XiaomiNdefPayloadType
import lib.xfy9326.xiaomi.nfc.XiaomiNfcPayload
import lib.xfy9326.xiaomi.nfc.decodeAsMiConnectPayload
import lib.xfy9326.xiaomi.nfc.getNfcProtocol
import lib.xfy9326.xiaomi.nfc.isValidNfcPayload
import lib.xfy9326.xiaomi.nfc.toXiaomiNfcPayload
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefReadData
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc
import tool.xfy9326.milink.nfc.utils.EMPTY
import tool.xfy9326.milink.nfc.utils.toHexString
import tool.xfy9326.milink.nfc.utils.toHexText
import java.text.SimpleDateFormat

class XiaomiNfcReaderViewModel : ViewModel() {
    enum class SnackbarMsg(@StringRes val resId: Int) {
        NDEF_RECORD_NOT_FOUND(R.string.xiaomi_ndef_not_found),
        NOT_XIAOMI_NFC(R.string.xiaomi_ndef_not_nfc),
        PARSE_ERROR(R.string.xiaomi_ndef_parse_error),
        VERSION_ERROR(R.string.xiaomi_ndef_version_error),
    }

    class NfcTagInfo(
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

    sealed interface NfcTagRecordUI

    class NfcTagDeviceRecordUI(
        val deviceType: String,
        val flags: String,
        val deviceNumber: String,
        val attributesMap: Map<String, String>,
    ) : NfcTagRecordUI {
        constructor(deviceRecord: NfcTagDeviceRecord, actionRecord: NfcTagActionRecord?, ndefPayloadType: XiaomiNdefPayloadType) : this(
            deviceType = deviceRecord.deviceType.name,
            flags = deviceRecord.flags.toHexString(true),
            deviceNumber = deviceRecord.deviceNumber.toHexString(true),
            attributesMap = if (actionRecord == null) {
                deviceRecord.attributesMap.map { it.key.toString() to it.value.toHexText() }
            } else {
                deviceRecord.getAllAttributesMap(actionRecord, ndefPayloadType).mapNotNull {
                    if (it.value.isNotEmpty()) {
                        val key = it.key.attributeName
                        val value = if (it.key == NfcTagDeviceRecord.DeviceAttribute.APP_DATA) {
                            when (NfcTagDeviceRecord.getAppDataValueType(it.value, actionRecord, ndefPayloadType)) {
                                NfcTagDeviceRecord.AppDataValueType.IOT_ACTION -> it.value.runCatching {
                                    toString(Charsets.UTF_8)
                                }.getOrNull() ?: it.value.toHexText()

                                else -> it.value.toHexText()
                            }
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

    class NfcTagActionRecordUI(
        val action: String,
        val condition: String,
        val deviceNumber: String,
        val flags: String,
        val conditionParameters: String
    ) : NfcTagRecordUI {
        constructor(actionRecord: NfcTagActionRecord) : this(
            action = actionRecord.action.name,
            condition = actionRecord.condition.name,
            deviceNumber = actionRecord.deviceNumber.toHexString(true),
            flags = actionRecord.flags.toHexString(true),
            conditionParameters = actionRecord.conditionParameters.toHexString(true),
        )
    }

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
                it.key.name to if (it.key == HandoffAppData.PayloadKey.EXT_ABILITY) {
                    it.value.toHexText()
                } else {
                    it.value.toString(Charsets.UTF_8)
                }
            }.toMap(),
        )
    }

    data class UiState(
        val tagInfo: NfcTagInfo? = null,
        val payloadUI: XiaomiNfcPayloadUI? = null,
        val handoffAppDataUI: HandoffAppDataUI? = null,
        val nfcTagAppDataUI: NfcTagAppDataUI? = null,
    ) {
        val hasData: Boolean = tagInfo != null && payloadUI != null && (handoffAppDataUI != null || nfcTagAppDataUI != null)
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _snackbarMsg = MutableSharedFlow<SnackbarMsg>()
    val snackbarMsg: SharedFlow<SnackbarMsg> = _snackbarMsg.asSharedFlow()

    fun updateNfcReadData(ndefReadData: NdefReadData) {
        viewModelScope.launch(Dispatchers.IO) {
            val type = XiaomiNfc.getXiaomiNfcPayloadType(ndefReadData.msg)
            if (type == null) {
                _snackbarMsg.emit(SnackbarMsg.NDEF_RECORD_NOT_FOUND)
                _uiState.update { it.copy() }
                return@launch
            }
            val bytes = XiaomiNfc.getXiaomiNfcPayloadBytes(ndefReadData.msg, type)
            if (bytes == null) {
                _snackbarMsg.emit(SnackbarMsg.NDEF_RECORD_NOT_FOUND)
                _uiState.update { it.copy() }
                return@launch
            }
            if (!decodeXiaomiNfcPayload(NfcTagInfo(type, ndefReadData), bytes)) {
                _uiState.update { it.copy() }
            }
        }
    }

    private suspend fun decodeXiaomiNfcPayload(tagInfo: NfcTagInfo, payloadBytes: ByteArray): Boolean {
        val miConnectPayload = runCatching { payloadBytes.decodeAsMiConnectPayload() }.getOrNull()
        if (miConnectPayload == null) {
            _snackbarMsg.emit(SnackbarMsg.PARSE_ERROR)
            return false
        }
        if (!miConnectPayload.isValidNfcPayload()) {
            _snackbarMsg.emit(SnackbarMsg.NOT_XIAOMI_NFC)
            return false
        }
        val protocol = runCatching { miConnectPayload.getNfcProtocol() }.getOrNull()
        if (protocol == null) {
            _snackbarMsg.emit(SnackbarMsg.VERSION_ERROR)
            return false
        }
        val payload = runCatching { miConnectPayload.toXiaomiNfcPayload(protocol) }.getOrNull()
        if (payload == null) {
            _snackbarMsg.emit(SnackbarMsg.PARSE_ERROR)
            return false
        }

        try {
            val payloadUI = XiaomiNfcPayloadUI(payload)
            when (val appsData = payload.appsData) {
                is HandoffAppData -> _uiState.update {
                    it.copy(tagInfo = tagInfo, payloadUI = payloadUI, handoffAppDataUI = HandoffAppDataUI(appsData))
                }

                is NfcTagAppData -> _uiState.update {
                    it.copy(tagInfo = tagInfo, payloadUI = payloadUI, nfcTagAppDataUI = NfcTagAppDataUI(appsData, tagInfo.ndefPayloadType))
                }
            }
            return true
        } catch (e: Exception) {
            // Ignore
        }
        return false
    }
}