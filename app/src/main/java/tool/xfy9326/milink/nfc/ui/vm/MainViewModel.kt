package tool.xfy9326.milink.nfc.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.datastore.AppDataStore
import tool.xfy9326.milink.nfc.datastore.base.key.readValue
import tool.xfy9326.milink.nfc.datastore.base.key.writeValue
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc
import tool.xfy9326.milink.nfc.utils.EmptyNdefMessage
import tool.xfy9326.milink.nfc.utils.isXiaomiHyperOS

class MainViewModel : ViewModel() {
    companion object {
        private const val PERMITS_NFC_USING = 1
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

    fun requestClearNdefWriteDialog() {
        _uiState.update {
            it.copy(ndefWriteDialogData = NdefWriteData(EmptyNdefMessage, false))
        }
    }

    fun requestFormatXiaomiTapNdefWriteDialog() {
        _uiState.update {
            it.copy(ndefWriteDialogData = NdefWriteData(XiaomiNfc.EmptyMiTap.newNdefMessage(Unit), false))
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