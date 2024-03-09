package tool.xfy9326.milink.nfc.ui.vm

import android.net.Uri
import android.nfc.NfcAdapter
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
import tool.xfy9326.milink.nfc.AppContext
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
import tool.xfy9326.milink.nfc.utils.requireEnabled

class MainViewModel : ViewModel() {
    enum class InstantMsg(@StringRes val resId: Int) {
        IMPORT_FAILED(R.string.import_failed),
        NDEF_PARSE_FAILED(R.string.ndef_parse_failed),
        NOT_SUPPORT_NFC(R.string.not_support_nfc),
        NFC_DISABLED(R.string.nfc_disabled)
    }

    data class UiState(
        val showNotSupportedOSDialog: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _instantMsg = MutableSharedFlow<InstantMsg>()
    val instantMsg: SharedFlow<InstantMsg> = _instantMsg.asSharedFlow()

    private val _nfcWriteData = MutableSharedFlow<NdefWriteData>()
    val nfcWriteData: SharedFlow<NdefWriteData> = _nfcWriteData.asSharedFlow()

    init {
        checkNotSupportedOS()
    }

    private fun checkNotSupportedOS() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!isXiaomiHyperOS() && !AppDataStore.readValue(AppDataStore.confirmedNotSupportedOSAlert)) {
                _uiState.update {
                    it.copy(showNotSupportedOSDialog = true)
                }
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

    fun requestNdefBinWriteActivity(uri: Uri) {
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

            requestNdefWriteActivity(NdefWriteData(msg = ndefMsg, readOnly = false))
        }
    }

    fun requestClearNdefWriteActivity() {
        requestNdefWriteActivity(NdefWriteData(msg = EmptyNdefMessage, readOnly = false))
    }

    fun requestFormatXiaomiTapNdefActivity() {
        viewModelScope.launch {
            val ndefMsg = XiaomiNfc.EmptyMiTap.newNdefMessage(
                (System.currentTimeMillis() / 1000).toInt(),
                AppDataStore.shrinkNdefMsg.getValue()
            )
            requestNdefWriteActivity(NdefWriteData(ndefMsg, false))
        }
    }

    fun requestNdefWriteActivity(ndefWriteData: NdefWriteData) {
        viewModelScope.launch {
            val nfcAdapter = NfcAdapter.getDefaultAdapter(AppContext)
            if (nfcAdapter == null) {
                _instantMsg.emit(InstantMsg.NOT_SUPPORT_NFC)
            } else if (!nfcAdapter.requireEnabled()) {
                _instantMsg.emit(InstantMsg.NFC_DISABLED)
            } else {
                _nfcWriteData.emit(ndefWriteData)
            }
        }
    }
}