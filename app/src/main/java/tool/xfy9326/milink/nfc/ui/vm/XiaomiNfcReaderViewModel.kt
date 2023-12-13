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
import lib.xfy9326.xiaomi.nfc.NfcTagAppData
import lib.xfy9326.xiaomi.nfc.decodeAsMiConnectPayload
import lib.xfy9326.xiaomi.nfc.getNfcProtocol
import lib.xfy9326.xiaomi.nfc.isValidNfcPayload
import lib.xfy9326.xiaomi.nfc.toXiaomiNfcPayload
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefReadData
import tool.xfy9326.milink.nfc.data.ui.AppDataUI
import tool.xfy9326.milink.nfc.data.ui.HandoffAppDataUI
import tool.xfy9326.milink.nfc.data.ui.NfcTagAppDataUI
import tool.xfy9326.milink.nfc.data.ui.XiaomiNfcPayloadUI
import tool.xfy9326.milink.nfc.data.ui.XiaomiNfcTagUI
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc

class XiaomiNfcReaderViewModel : ViewModel() {
    enum class InstantMsg(@StringRes val resId: Int, val isToast: Boolean = false) {
        NEW_TAG_FOUND(R.string.nfc_new_tag_found, true),
        NDEF_RECORD_NOT_FOUND(R.string.xiaomi_ndef_not_found),
        NOT_XIAOMI_NFC(R.string.xiaomi_ndef_not_nfc),
        PARSE_ERROR(R.string.xiaomi_ndef_parse_error),
        VERSION_ERROR(R.string.xiaomi_ndef_version_error),
    }

    data class UiState(
        val nfcInfo: NfcInfo? = null
    ) {
        data class NfcInfo(
            val tag: XiaomiNfcTagUI,
            val payload: XiaomiNfcPayloadUI,
            val appData: AppDataUI,
        )
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _instantMsg = MutableSharedFlow<InstantMsg>()
    val instantMsg: SharedFlow<InstantMsg> = _instantMsg.asSharedFlow()

    fun updateNfcReadData(ndefReadData: NdefReadData) {
        viewModelScope.launch(Dispatchers.IO) {
            val type = XiaomiNfc.getXiaomiNfcPayloadType(ndefReadData.msg)
            if (type == null) {
                _instantMsg.emit(InstantMsg.NDEF_RECORD_NOT_FOUND)
                _uiState.update { it.copy() }
                return@launch
            }
            val bytes = XiaomiNfc.getXiaomiNfcPayloadBytes(ndefReadData.msg, type)
            if (bytes == null) {
                _instantMsg.emit(InstantMsg.NDEF_RECORD_NOT_FOUND)
                _uiState.update { it.copy() }
                return@launch
            }
            if (!decodeXiaomiNfcPayload(XiaomiNfcTagUI(type, ndefReadData), bytes)) {
                _uiState.update { it.copy() }
            }
        }
    }

    private suspend fun decodeXiaomiNfcPayload(tagInfo: XiaomiNfcTagUI, payloadBytes: ByteArray): Boolean {
        val miConnectPayload = runCatching { payloadBytes.decodeAsMiConnectPayload() }.getOrNull()
        if (miConnectPayload == null) {
            _instantMsg.emit(InstantMsg.PARSE_ERROR)
            return false
        }
        if (!miConnectPayload.isValidNfcPayload()) {
            _instantMsg.emit(InstantMsg.NOT_XIAOMI_NFC)
            return false
        }
        val protocol = runCatching { miConnectPayload.getNfcProtocol() }.getOrNull()
        if (protocol == null) {
            _instantMsg.emit(InstantMsg.VERSION_ERROR)
            return false
        }
        val payload = runCatching { miConnectPayload.toXiaomiNfcPayload(protocol) }.getOrNull()
        if (payload == null) {
            _instantMsg.emit(InstantMsg.PARSE_ERROR)
            return false
        }

        try {
            val info = UiState.NfcInfo(
                tag = tagInfo,
                payload = XiaomiNfcPayloadUI(payload),
                appData = when (val appData = payload.appsData) {
                    is HandoffAppData -> HandoffAppDataUI(appData)
                    is NfcTagAppData -> NfcTagAppDataUI(appData, tagInfo.ndefPayloadType)
                }
            )
            _uiState.update { it.copy(nfcInfo = info) }
            _instantMsg.emit(InstantMsg.NEW_TAG_FOUND)
            return true
        } catch (e: Exception) {
            // Ignore
        }
        return false
    }
}