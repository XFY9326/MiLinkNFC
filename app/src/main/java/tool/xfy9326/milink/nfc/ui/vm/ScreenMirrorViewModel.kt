package tool.xfy9326.milink.nfc.ui.vm

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
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
import tool.xfy9326.milink.nfc.data.HuaweiRedirect
import tool.xfy9326.milink.nfc.data.NdefData
import tool.xfy9326.milink.nfc.data.NfcActionIntentType
import tool.xfy9326.milink.nfc.data.ScreenMirror
import tool.xfy9326.milink.nfc.datastore.AppDataStore
import tool.xfy9326.milink.nfc.datastore.base.key.readValue
import tool.xfy9326.milink.nfc.datastore.base.key.writeValue
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc
import tool.xfy9326.milink.nfc.service.ScreenMirrorTileService
import tool.xfy9326.milink.nfc.ui.dialog.MiLinkVersionDialogData
import tool.xfy9326.milink.nfc.utils.EMPTY
import tool.xfy9326.milink.nfc.utils.MiContinuityUtils
import tool.xfy9326.milink.nfc.utils.isValidMacAddress
import tool.xfy9326.milink.nfc.utils.isXiaomiHyperOS

class ScreenMirrorViewModel : ViewModel() {
    enum class SnackbarMsg(@StringRes val resId: Int) {
        INVALID_MAC_ADDRESS(R.string.invalid_mac_address),
        EMPTY_MAC_ADDRESS(R.string.empty_mac_address),
        SAVE_SUCCESS(R.string.save_success),
        NFC_USING_CONFLICT(R.string.nfc_using_conflict),
        TILES_ADD_SUCCESS(R.string.tiles_add_success),
        TILES_ALREADY_ADD(R.string.tiles_already_added),
        TILES_ADD_FAILED(R.string.tiles_add_failed);
    }

    data class UiState(
        val showNotSupportedOSDialog: Boolean = false,
        val screenMirrorNFCTag: ScreenMirror.NFCTag = ScreenMirror.NFCTag(
            deviceType = ScreenMirror.DeviceType.PC,
            bluetoothMac = EMPTY,
            readOnly = false,
            enableLyra = true
        ),
        val testScreenMirror: ScreenMirror = ScreenMirror(
            deviceType = ScreenMirror.DeviceType.PC,
            actionIntentType = NfcActionIntentType.FAKE_NFC_TAG,
            bluetoothMac = EMPTY,
            enableLyra = true
        ),
        val tilesScreenMirror: ScreenMirror = AppDataStore.Defaults.tilesScreenMirror,
        val huaweiRedirect: HuaweiRedirect = AppDataStore.Defaults.huaweiRedirect,
        val ndefWriteDialogData: NdefData? = null,
        val miLinkPackageDialogData: MiLinkVersionDialogData? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _snackbarMsg = MutableSharedFlow<SnackbarMsg>()
    val snackbarMsg: SharedFlow<SnackbarMsg> = _snackbarMsg.asSharedFlow()

    init {
        checkLyraSupport()
        viewModelScope.launch(Dispatchers.IO) {
            checkNotSupportedOS()
            initDataStoreListeners()
        }
    }

    private suspend fun validateBluetoothMac(btMac: String): Boolean {
        if (btMac.isBlank()) {
            _snackbarMsg.emit(SnackbarMsg.EMPTY_MAC_ADDRESS)
        } else if (!btMac.isValidMacAddress()) {
            _snackbarMsg.emit(SnackbarMsg.INVALID_MAC_ADDRESS)
        } else {
            return true
        }
        return false
    }

    private fun checkLyraSupport() {
        if (!MiContinuityUtils.isLocalDeviceSupportLyra(AppContext)) {
            _uiState.update {
                it.copy(
                    screenMirrorNFCTag = it.screenMirrorNFCTag.copy(enableLyra = false),
                    testScreenMirror = it.testScreenMirror.copy(enableLyra = false)
                )
            }
        }
    }

    private suspend fun checkNotSupportedOS() {
        if (!isXiaomiHyperOS() && !AppDataStore.readValue(AppDataStore.confirmedNotSupportedOSAlert)) {
            _uiState.update {
                it.copy(showNotSupportedOSDialog = true)
            }
        }
    }

    private suspend fun initDataStoreListeners() = coroutineScope {
        launch {
            AppDataStore.getTilesScreenMirror().collect { data ->
                _uiState.update { it.copy(tilesScreenMirror = data) }
            }
        }
        launch {
            AppDataStore.getHuaweiRedirect().collect { data ->
                _uiState.update { it.copy(huaweiRedirect = data) }
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

    fun requestWriteNfc(nfcTagData: ScreenMirror.NFCTag) {
        viewModelScope.launch {
            if (validateBluetoothMac(nfcTagData.bluetoothMac)) {
                val data = NdefData(XiaomiNfc.ScreenMirror.newNdefMessage(nfcTagData.toConfig()), nfcTagData.readOnly)
                _uiState.update {
                    it.copy(ndefWriteDialogData = data)
                }
            }
        }
    }

    fun reportNfcDeviceUsing() {
        viewModelScope.launch {
            _snackbarMsg.emit(SnackbarMsg.NFC_USING_CONFLICT)
        }
    }

    fun cancelWriteNfc() {
        _uiState.update {
            it.copy(ndefWriteDialogData = null)
        }
    }

    fun openMiLinkVersionDialog() {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update {
                val data = MiLinkVersionDialogData(
                    lyraSupported = MiContinuityUtils.isLocalDeviceSupportLyra(AppContext),
                    packageData = MiContinuityUtils.getNfcRelatedPackageDataMap(AppContext)
                )
                it.copy(miLinkPackageDialogData = data)
            }
        }
    }

    fun closeMiLinkVersionDialog() {
        _uiState.update {
            it.copy(miLinkPackageDialogData = null)
        }
    }

    fun sendScreenMirror(context: Context, screenMirror: ScreenMirror) {
        viewModelScope.launch {
            if (validateBluetoothMac(screenMirror.bluetoothMac)) {
                val config = screenMirror.toConfig()
                when (screenMirror.actionIntentType) {
                    NfcActionIntentType.FAKE_NFC_TAG -> XiaomiNfc.ScreenMirror.newNdefDiscoveredIntent(null, null, config).also {
                        ContextCompat.startActivity(context, it, null)
                    }

                    NfcActionIntentType.MI_CONNECT_SERVICE -> XiaomiNfc.ScreenMirror.sendBroadcast(context, config)
                }
            }
        }
    }

    fun requestAddTiles(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService<StatusBarManager>()?.requestAddTileService(
                ComponentName(context, ScreenMirrorTileService::class.java),
                context.getString(R.string.tiles_screen_mirror_service),
                Icon.createWithResource(context, R.drawable.ic_screen_share_24),
                { it.run() },
                {
                    viewModelScope.launch {
                        if (it == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED) {
                            _snackbarMsg.emit(SnackbarMsg.TILES_ADD_SUCCESS)
                        } else if (it == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED) {
                            _snackbarMsg.emit(SnackbarMsg.TILES_ALREADY_ADD)
                        } else if (it != StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED) {
                            _snackbarMsg.emit(SnackbarMsg.TILES_ADD_FAILED)
                        }
                    }
                }
            )
        }
    }

    fun updateTilesScreenMirror(screenMirror: ScreenMirror) {
        _uiState.update {
            it.copy(tilesScreenMirror = screenMirror)
        }
    }

    fun saveTilesScreenMirror() {
        viewModelScope.launch(Dispatchers.IO) {
            val screenMirror = uiState.value.tilesScreenMirror
            if (validateBluetoothMac(screenMirror.bluetoothMac)) {
                AppDataStore.setTilesScreenMirror(screenMirror)
                _snackbarMsg.emit(SnackbarMsg.SAVE_SUCCESS)
            }
        }
    }

    fun updateHuaweiRedirect(huaweiRedirect: HuaweiRedirect) {
        _uiState.update {
            it.copy(huaweiRedirect = huaweiRedirect)
        }
    }

    fun saveHuaweiRedirect() {
        viewModelScope.launch(Dispatchers.IO) {
            AppDataStore.setHuaweiRedirect(uiState.value.huaweiRedirect)
            _snackbarMsg.emit(SnackbarMsg.SAVE_SUCCESS)
        }
    }
}