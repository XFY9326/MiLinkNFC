package tool.xfy9326.milink.nfc.ui.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tool.xfy9326.milink.nfc.data.NdefData

class MiPlayViewModel : ViewModel() {
    data class UiState(
        val ndefWriteDialogData: NdefData? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
}