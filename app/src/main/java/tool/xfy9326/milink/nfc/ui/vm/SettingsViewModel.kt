package tool.xfy9326.milink.nfc.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import tool.xfy9326.milink.nfc.data.AppSettings
import tool.xfy9326.milink.nfc.datastore.AppDataStore

class SettingsViewModel : ViewModel() {
    data class UiState(
        val appSettings: AppSettings = AppDataStore.Defaults.appSettings
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        AppDataStore.getAppSettings().onEach { data ->
            _uiState.update { it.copy(appSettings = data) }
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun updateAppSettings(appSettings: AppSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDataStore.setAppSettings(appSettings)
        }
    }
}