package tool.xfy9326.milink.nfc.ui.vm

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.parcelize.Parcelize
import tool.xfy9326.milink.nfc.AppContext
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.HuaweiRedirect
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.data.NfcActionIntentType
import tool.xfy9326.milink.nfc.data.ScreenMirror
import tool.xfy9326.milink.nfc.datastore.AppDataStore
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc
import tool.xfy9326.milink.nfc.service.ScreenMirrorTileService
import tool.xfy9326.milink.nfc.ui.dialog.MiLinkVersionDialogData
import tool.xfy9326.milink.nfc.utils.EMPTY
import tool.xfy9326.milink.nfc.utils.MiContinuityUtils
import tool.xfy9326.milink.nfc.utils.isValidMacAddress

class ScreenMirrorViewModel : ViewModel() {
    enum class InstantMsg(@StringRes val resId: Int) {
        INVALID_MAC_ADDRESS(R.string.invalid_mac_address),
        EMPTY_MAC_ADDRESS(R.string.empty_mac_address),
        SAVE_SUCCESS(R.string.save_success),
        TILES_ADD_SUCCESS(R.string.tiles_add_success),
        TILES_ALREADY_ADD(R.string.tiles_already_added),
        TILES_ADD_FAILED(R.string.tiles_add_failed);
    }

    @Parcelize
    data class NFCTag(
        val deviceType: ScreenMirror.DeviceType = ScreenMirror.DeviceType.PC,
        val bluetoothMac: String = EMPTY,
        val enableLyra: Boolean = true,
        val readOnly: Boolean = false
    ) : Parcelable {
        fun toConfig() =
            XiaomiNfc.ScreenMirror.Config(
                deviceType = deviceType.handOffType,
                bluetoothMac = bluetoothMac,
                enableLyra = enableLyra
            )
    }

    data class UiState(
        val nfcTag: NFCTag = NFCTag(),
        val testScreenMirror: ScreenMirror = ScreenMirror(
            deviceType = ScreenMirror.DeviceType.PC,
            actionIntentType = NfcActionIntentType.FAKE_NFC_TAG,
            bluetoothMac = EMPTY,
            enableLyra = true
        ),
        val tilesScreenMirror: ScreenMirror = AppDataStore.Defaults.tilesScreenMirror,
        val huaweiRedirect: HuaweiRedirect = AppDataStore.Defaults.huaweiRedirect,
        val miLinkPackageDialogData: MiLinkVersionDialogData? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _instantMsg = MutableSharedFlow<InstantMsg>()
    val instantMsg: SharedFlow<InstantMsg> = _instantMsg.asSharedFlow()

    init {
        checkLyraSupport()
        initDataStoreListeners()
    }

    private suspend fun validateMacAddress(bluetoothMac: String): Boolean {
        if (bluetoothMac.isBlank()) {
            _instantMsg.emit(InstantMsg.EMPTY_MAC_ADDRESS)
        } else if (!bluetoothMac.isValidMacAddress()) {
            _instantMsg.emit(InstantMsg.INVALID_MAC_ADDRESS)
        } else {
            return true
        }
        return false
    }

    private fun checkLyraSupport() {
        viewModelScope.launch {
            if (!MiContinuityUtils.isLocalDeviceSupportLyra(AppContext)) {
                _uiState.update {
                    it.copy(
                        nfcTag = it.nfcTag.copy(enableLyra = false),
                        testScreenMirror = it.testScreenMirror.copy(enableLyra = false)
                    )
                }
            }
        }
    }

    private fun initDataStoreListeners() {
        AppDataStore.getTilesScreenMirror().onEach { data ->
            _uiState.update { it.copy(tilesScreenMirror = data) }
        }.launchIn(viewModelScope + Dispatchers.IO)
        AppDataStore.getHuaweiRedirect().onEach { data ->
            _uiState.update { it.copy(huaweiRedirect = data) }
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun requestWriteNfc(nfcTagData: NFCTag, ndefWriteHandler: (NdefWriteData) -> Unit) {
        viewModelScope.launch {
            if (validateMacAddress(nfcTagData.bluetoothMac)) {
                val ndefMsg = XiaomiNfc.ScreenMirror.newNdefMessage(
                    nfcTagData.toConfig(),
                    AppDataStore.shrinkNdefMsg.getValue()
                )
                val data = NdefWriteData(ndefMsg, nfcTagData.readOnly)
                ndefWriteHandler(data)
            }
        }
    }

    fun openMiLinkVersionDialog(context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update {
                val data = MiLinkVersionDialogData(
                    lyraSupported = MiContinuityUtils.isLocalDeviceSupportLyra(context),
                    packageData = MiContinuityUtils.getNfcRelatedPackageDataMap(context)
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
            if (validateMacAddress(screenMirror.bluetoothMac)) {
                val config = screenMirror.toConfig()
                when (screenMirror.actionIntentType) {
                    NfcActionIntentType.FAKE_NFC_TAG -> XiaomiNfc.ScreenMirror.newNdefDiscoveredIntent(
                        null,
                        null,
                        config
                    ).also {
                        ContextCompat.startActivity(context, it, null)
                    }

                    NfcActionIntentType.MI_CONNECT_SERVICE -> XiaomiNfc.ScreenMirror.sendBroadcast(
                        context,
                        config
                    )
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
                            _instantMsg.emit(InstantMsg.TILES_ADD_SUCCESS)
                        } else if (it == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED) {
                            _instantMsg.emit(InstantMsg.TILES_ALREADY_ADD)
                        } else if (it != StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED) {
                            _instantMsg.emit(InstantMsg.TILES_ADD_FAILED)
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
            if (validateMacAddress(screenMirror.bluetoothMac)) {
                AppDataStore.setTilesScreenMirror(screenMirror)
                _instantMsg.emit(InstantMsg.SAVE_SUCCESS)
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
            _instantMsg.emit(InstantMsg.SAVE_SUCCESS)
        }
    }
}