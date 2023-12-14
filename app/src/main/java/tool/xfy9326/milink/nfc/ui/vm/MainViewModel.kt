package tool.xfy9326.milink.nfc.ui.vm

import android.net.Uri
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
import kotlinx.coroutines.sync.Semaphore
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.datastore.AppDataStore
import tool.xfy9326.milink.nfc.datastore.base.key.readValue
import tool.xfy9326.milink.nfc.datastore.base.key.writeValue
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc
import tool.xfy9326.milink.nfc.utils.EmptyNdefMessage
import tool.xfy9326.milink.nfc.utils.NdefIO
import tool.xfy9326.milink.nfc.utils.isXiaomiHyperOS
import tool.xfy9326.milink.nfc.utils.readBinary

class MainViewModel : ViewModel() {
    companion object {
        private const val PERMITS_NFC_USING = 1
    }

    enum class InstantMsg(@StringRes val resId: Int) {
        IMPORT_FAILED(R.string.import_failed),
        NDEF_PARSE_FAILED(R.string.ndef_parse_failed),
    }

    data class UiState(
        val showNotSupportedOSDialog: Boolean = false,
        val ndefWriteDialogData: NdefWriteData? = null,
    )

    private val nfcUsing = Semaphore(PERMITS_NFC_USING)

    private val _nfcWriteData = MutableStateFlow<NdefWriteData?>(null)
    val nfcWriteData: StateFlow<NdefWriteData?> = _nfcWriteData.asStateFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _instantMsg = MutableSharedFlow<InstantMsg>()
    val instantMsg: SharedFlow<InstantMsg> = _instantMsg.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            checkNotSupportedOS()
        }
    }

    private suspend fun checkNotSupportedOS() {
        if (!isXiaomiHyperOS() && !AppDataStore.readValue(AppDataStore.confirmedNotSupportedOSAlert)) {
            _uiState.update {
                it.copy(showNotSupportedOSDialog = true)
            }
        }
    }

    fun confirmNotSupportedOS() {
        viewModelScope.launch(Dispatchers.IO) {
            AppDataStore.writeValue(AppDataStore.confirmedNotSupportedOSAlert, true)
            _uiState.update {
                it.copy(showNotSupportedOSDialog = false)
            }
        }
    }

    fun requestNdefBinWriteDialog(uri: Uri) {
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

            requestNdefWriteDialog(NdefWriteData(msg = ndefMsg, readOnly = false))
        }
    }

    fun requestClearNdefWriteDialog() {
        _uiState.update {
            it.copy(ndefWriteDialogData = NdefWriteData(EmptyNdefMessage, false))
        }
    }

    fun requestFormatXiaomiTapNdefWriteDialog() {
        viewModelScope.launch(Dispatchers.IO) {
            val ndefMsg = XiaomiNfc.EmptyMiTap.newNdefMessage(Unit, AppDataStore.shrinkNdefMsg.getValue())
            _uiState.update {
                it.copy(ndefWriteDialogData = NdefWriteData(ndefMsg, false))
            }
        }
    }

    fun requestNdefWriteDialog(ndefWriteData: NdefWriteData) {
        _uiState.update {
            it.copy(ndefWriteDialogData = ndefWriteData)
        }
    }

    fun cancelNdefWriteDialog() {
        _uiState.update {
            it.copy(ndefWriteDialogData = null)
        }
    }

    fun openNFCWriter(ndefWriteData: NdefWriteData): Boolean {
        return if (nfcUsing.tryAcquire()) {
            _nfcWriteData.update { ndefWriteData }
            true
        } else {
            false
        }
    }

    fun closeNfcWriter() {
        _nfcWriteData.update { null }
        try {
            if (nfcUsing.availablePermits < PERMITS_NFC_USING) {
                nfcUsing.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}