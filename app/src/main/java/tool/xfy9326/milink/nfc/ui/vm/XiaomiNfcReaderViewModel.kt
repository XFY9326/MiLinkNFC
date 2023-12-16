package tool.xfy9326.milink.nfc.ui.vm

import android.net.Uri
import android.nfc.NdefMessage
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
import lib.xfy9326.xiaomi.nfc.XiaomiNdefPayloadType
import lib.xfy9326.xiaomi.nfc.decodeAsMiConnectPayload
import lib.xfy9326.xiaomi.nfc.getNfcProtocol
import lib.xfy9326.xiaomi.nfc.isValidNfcPayload
import lib.xfy9326.xiaomi.nfc.toXiaomiNfcPayload
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefReadData
import tool.xfy9326.milink.nfc.data.ui.AppDataUI
import tool.xfy9326.milink.nfc.data.ui.HandoffAppDataUI
import tool.xfy9326.milink.nfc.data.ui.NfcTagAppDataUI
import tool.xfy9326.milink.nfc.data.ui.NfcTagInfoUI
import tool.xfy9326.milink.nfc.data.ui.XiaomiNfcPayloadUI
import tool.xfy9326.milink.nfc.datastore.AppDataStore
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc
import tool.xfy9326.milink.nfc.utils.NdefIO
import tool.xfy9326.milink.nfc.utils.readBinary

class XiaomiNfcReaderViewModel : ViewModel() {
    enum class InstantMsg(@StringRes val resId: Int) {
        NEW_TAG_FOUND(R.string.nfc_new_tag_found),
        EXPORT_SUCCESS(R.string.export_success),
        EXPORT_FAILED(R.string.export_failed),
        IMPORT_SUCCESS(R.string.import_success),
        IMPORT_FAILED(R.string.import_failed),
        NDEF_PARSE_FAILED(R.string.ndef_parse_failed),
        NDEF_RECORD_NOT_FOUND(R.string.xiaomi_ndef_not_found),
        NOT_XIAOMI_NFC(R.string.xiaomi_ndef_not_nfc),
        PARSE_ERROR(R.string.xiaomi_ndef_parse_error),
        CONTENT_PARSE_ERROR(R.string.xiaomi_ndef_content_parse_error),
        VERSION_ERROR(R.string.xiaomi_ndef_version_error),
        NO_CACHED_NDEF_DATA(R.string.no_cached_ndef_data);
    }

    data class UiState(
        val canExportNdefBin: Boolean = false,
        val nfcInfo: NfcInfo? = null
    ) {
        data class NfcInfo(
            val tag: NfcTagInfoUI?,
            val ndefType: XiaomiNdefPayloadType,
            val payload: XiaomiNfcPayloadUI,
            val appData: AppDataUI,
        )
    }

    private class NdefMsgCache(
        val scanTime: Long,
        val content: ByteArray
    )

    private var ndefMsgCache: NdefMsgCache? = null

    private val _exportNdefBin = MutableSharedFlow<String>()
    val exportNdefBin: SharedFlow<String> = _exportNdefBin.asSharedFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _instantMsg = MutableSharedFlow<InstantMsg>()
    val instantMsg: SharedFlow<InstantMsg> = _instantMsg.asSharedFlow()

    private suspend fun prepareNdefReadCache(): NdefMsgCache? {
        val cache = ndefMsgCache
        if (cache == null) {
            _instantMsg.emit(InstantMsg.NO_CACHED_NDEF_DATA)
        }
        return cache
    }

    fun requestExportNdefBin() {
        viewModelScope.launch {
            prepareNdefReadCache()?.let {
                val fileName = NdefIO.getExportFileName(it.scanTime, AppDataStore.exportNxpNdefFormat.getValue())
                _exportNdefBin.emit(fileName)
            }
        }
    }

    fun exportNdefBin(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            prepareNdefReadCache()?.let {
                val result = NdefIO.writeNdefMessage(uri, it.content, AppDataStore.exportNxpNdefFormat.getValue())
                _instantMsg.emit(if (result) InstantMsg.EXPORT_SUCCESS else InstantMsg.EXPORT_FAILED)
            }
        }
    }

    fun updateNfcReadData(ndefReadData: NdefReadData) {
        viewModelScope.launch(Dispatchers.IO) {
            ndefMsgCache = NdefMsgCache(ndefReadData.scanTime, ndefReadData.msg.toByteArray())
            _uiState.update { it.copy(canExportNdefBin = true) }

            val tagInfo = NfcTagInfoUI(ndefReadData)

            if (decodeXiaomiNfcPayload(tagInfo, ndefReadData.msg)) {
                _instantMsg.emit(InstantMsg.NEW_TAG_FOUND)
            } else {
                _uiState.update { it.copy(nfcInfo = null) }
            }
        }
    }

    fun updateNfcReadData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = uri.readBinary()
            if (bytes == null) {
                _instantMsg.emit(InstantMsg.IMPORT_FAILED)
                return@launch
            }

            val ndefMsg = NdefIO.readNdefMessage(bytes)
            if (ndefMsg == null) {
                _instantMsg.emit(InstantMsg.NDEF_PARSE_FAILED)
                return@launch
            }

            ndefMsgCache = NdefMsgCache(System.currentTimeMillis(), bytes)
            _uiState.update { it.copy(canExportNdefBin = true) }

            if (decodeXiaomiNfcPayload(null, ndefMsg)) {
                _instantMsg.emit(InstantMsg.IMPORT_SUCCESS)
            } else {
                _uiState.update { it.copy(nfcInfo = null) }
            }
        }
    }

    fun clearNfcReadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(nfcInfo = null, canExportNdefBin = false) }
            ndefMsgCache = null
        }
    }

    private suspend fun decodeXiaomiNfcPayload(tagInfo: NfcTagInfoUI?, ndefMessage: NdefMessage): Boolean {
        val ndefType = XiaomiNfc.getXiaomiNfcPayloadType(ndefMessage)
        if (ndefType == null) {
            _instantMsg.emit(InstantMsg.NDEF_RECORD_NOT_FOUND)
            return false
        }
        val ndefBytes = XiaomiNfc.getXiaomiNfcPayloadBytes(ndefMessage, ndefType)
        if (ndefBytes == null) {
            _instantMsg.emit(InstantMsg.NDEF_RECORD_NOT_FOUND)
            return false
        }
        val miConnectPayload = runCatching { ndefBytes.decodeAsMiConnectPayload() }.getOrNull()
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
            _instantMsg.emit(InstantMsg.CONTENT_PARSE_ERROR)
            return false
        }

        try {
            val info = UiState.NfcInfo(
                tag = tagInfo,
                ndefType = ndefType,
                payload = XiaomiNfcPayloadUI(payload),
                appData = when (val appData = payload.appsData) {
                    is HandoffAppData -> HandoffAppDataUI(appData)
                    is NfcTagAppData -> NfcTagAppDataUI(appData, ndefType)
                }
            )
            _uiState.update { it.copy(nfcInfo = info) }
            return true
        } catch (e: Exception) {
            // Ignore
        }
        return false
    }
}