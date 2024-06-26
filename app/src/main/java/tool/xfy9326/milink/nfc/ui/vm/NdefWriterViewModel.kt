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
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.datastore.AppDataStore
import tool.xfy9326.milink.nfc.utils.NdefIO

class NdefWriterViewModel : ViewModel() {
    enum class InstantMsg(@StringRes val resId: Int) {
        EXPORT_SUCCESS(R.string.export_success),
        EXPORT_FAILED(R.string.export_failed),
    }

    data class UiState(
        val isWriting: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _exportNdefBin = MutableSharedFlow<String>()
    val exportNdefBin: SharedFlow<String> = _exportNdefBin.asSharedFlow()

    private val _instantMsg = MutableSharedFlow<InstantMsg>()
    val instantMsg: SharedFlow<InstantMsg> = _instantMsg.asSharedFlow()

    fun setWritingStatus(isWriting: Boolean) {
        _uiState.update { it.copy(isWriting = isWriting) }
    }

    fun requestExportNdefBin() {
        viewModelScope.launch {
            val fileName = NdefIO.getExportFileName(
                System.currentTimeMillis(),
                AppDataStore.exportNxpNdefFormat.getValue()
            )
            _exportNdefBin.emit(fileName)
        }
    }

    fun exportNdefBin(uri: Uri, buffer: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            val result =
                NdefIO.writeNdefMessage(uri, buffer, AppDataStore.exportNxpNdefFormat.getValue())
            _instantMsg.emit(if (result) InstantMsg.EXPORT_SUCCESS else InstantMsg.EXPORT_FAILED)
        }
    }
}