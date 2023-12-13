package tool.xfy9326.milink.nfc.ui.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tool.xfy9326.milink.nfc.data.NdefWriteData

class MiCirculateViewModel : ViewModel() {
    data class UiState(
        val ndefWriteDialogData: NdefWriteData? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
}