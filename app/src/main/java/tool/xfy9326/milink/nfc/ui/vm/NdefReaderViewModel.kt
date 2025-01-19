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
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.xfy9326.xiaomi.nfc.MiConnectData
import lib.xfy9326.xiaomi.nfc.XiaomiNdefType
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefReadData
import tool.xfy9326.milink.nfc.data.nfc.NdefData
import tool.xfy9326.milink.nfc.data.nfc.NfcTag
import tool.xfy9326.milink.nfc.data.nfc.XiaomiNdefData
import tool.xfy9326.milink.nfc.datastore.AppDataStore
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc
import tool.xfy9326.milink.nfc.utils.NdefIO
import tool.xfy9326.milink.nfc.utils.readBinary

class NdefReaderViewModel : ViewModel() {
    enum class InstantMsg(@StringRes val resId: Int) {
        NEW_TAG_FOUND(R.string.nfc_new_tag_found),
        EXPORT_SUCCESS(R.string.export_success),
        EXPORT_FAILED(R.string.export_failed),
        IMPORT_SUCCESS(R.string.import_success),
        IMPORT_FAILED(R.string.import_failed),
        NDEF_PARSE_FAILED(R.string.ndef_parse_failed),
        NOT_XIAOMI_NFC(R.string.xiaomi_ndef_not_nfc),
        PARSE_ERROR(R.string.xiaomi_ndef_parse_error),
        CONTENT_PARSE_ERROR(R.string.xiaomi_ndef_content_parse_error),
        VERSION_ERROR(R.string.xiaomi_ndef_version_error),
        NO_CACHED_NDEF_DATA(R.string.no_cached_ndef_data);
    }

    data class UiState(
        val canExportNdefBin: Boolean = false,
        val nfcTag: NfcTag? = null,
        val ndefRecords: List<NdefData> = emptyList()
    ) {
        val hasData: Boolean
            get() = nfcTag != null || ndefRecords.isNotEmpty()
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
                val fileName = NdefIO.getExportFileName(
                    it.scanTime,
                    AppDataStore.exportNxpNdefFormat.getValue()
                )
                _exportNdefBin.emit(fileName)
            }
        }
    }

    fun exportNdefBin(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            prepareNdefReadCache()?.let {
                val result = NdefIO.writeNdefMessage(
                    uri,
                    it.content,
                    AppDataStore.exportNxpNdefFormat.getValue()
                )
                _instantMsg.emit(if (result) InstantMsg.EXPORT_SUCCESS else InstantMsg.EXPORT_FAILED)
            }
        }
    }

    fun updateNfcReadData(ndefReadData: NdefReadData) {
        viewModelScope.launch(Dispatchers.IO) {
            ndefMsgCache = NdefMsgCache(ndefReadData.scanTime, ndefReadData.msg.toByteArray())
            _uiState.update {
                it.copy(
                    canExportNdefBin = true,
                    nfcTag = NfcTag(ndefReadData)
                )
            }

            handleNdefMessage(ndefReadData.msg)
            _instantMsg.emit(InstantMsg.NEW_TAG_FOUND)
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
            _uiState.update {
                it.copy(
                    canExportNdefBin = true,
                    nfcTag = null
                )
            }

            handleNdefMessage(ndefMsg)
            _instantMsg.emit(InstantMsg.IMPORT_SUCCESS)
        }
    }

    private suspend fun handleNdefMessage(ndefMessage: NdefMessage) {
        val records = ndefMessage.records.asFlow().filterNotNull().map {
            XiaomiNfc.getXiaomiNdefType(it)?.let { tnf ->
                decodeXiaomiNfc(tnf, it.payload)
            } ?: NdefData.parseWellKnown(it)
        }.toList()
        _uiState.update { it.copy(ndefRecords = records) }
    }

    fun clearNfcReadData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    nfcTag = null,
                    ndefRecords = emptyList(),
                    canExportNdefBin = false
                )
            }
            ndefMsgCache = null
        }
    }

    private suspend fun decodeXiaomiNfc(
        ndefType: XiaomiNdefType,
        ndefBytes: ByteArray
    ): XiaomiNdefData? {
        val miConnectData = runCatching {
            MiConnectData.parse(ndefBytes)
        }.onFailure {
            it.printStackTrace()
        }.getOrNull()
        if (miConnectData == null) {
            _instantMsg.emit(InstantMsg.PARSE_ERROR)
            return null
        }
        if (!miConnectData.isValidNfcPayload) {
            _instantMsg.emit(InstantMsg.NOT_XIAOMI_NFC)
            return null
        }
        val protocol = runCatching {
            miConnectData.getNfcProtocol()
        }.onFailure {
            it.printStackTrace()
        }.getOrNull()
        if (protocol == null) {
            _instantMsg.emit(InstantMsg.VERSION_ERROR)
            return null
        }
        val payload = runCatching {
            miConnectData.toXiaomiNfcPayload(protocol)
        }.onFailure {
            it.printStackTrace()
        }.getOrNull()
        if (payload == null) {
            _instantMsg.emit(InstantMsg.CONTENT_PARSE_ERROR)
            return null
        }

        return runCatching {
            XiaomiNdefData.parse(ndefType, payload)
        }.onFailure {
            it.printStackTrace()
        }.getOrNull()
    }
}