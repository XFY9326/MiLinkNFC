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
import com.google.protobuf.BoolValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tool.xfy9326.milink.nfc.AppContext
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.HuaweiRedirectData
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.data.XiaomiDeviceType
import tool.xfy9326.milink.nfc.data.XiaomiMirrorData
import tool.xfy9326.milink.nfc.data.XiaomiMirrorType
import tool.xfy9326.milink.nfc.data.XiaomiNFCTagData
import tool.xfy9326.milink.nfc.data.getHuaweiRedirectData
import tool.xfy9326.milink.nfc.data.getTilesXiaomiMirrorData
import tool.xfy9326.milink.nfc.data.toXiaomiDeviceType
import tool.xfy9326.milink.nfc.data.toXiaomiMirrorType
import tool.xfy9326.milink.nfc.db.AppSettings
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc
import tool.xfy9326.milink.nfc.service.MiShareTileService
import tool.xfy9326.milink.nfc.ui.dialog.MiLinkVersionDialogData
import tool.xfy9326.milink.nfc.utils.EMPTY
import tool.xfy9326.milink.nfc.utils.EmptyNdefMessage
import tool.xfy9326.milink.nfc.utils.MiContinuityUtils
import tool.xfy9326.milink.nfc.utils.isValidMacAddress
import tool.xfy9326.milink.nfc.utils.isXiaomiHyperOS

class NFCMirrorViewModel : ViewModel() {
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
        val defaultNFCTagData: XiaomiNFCTagData = XiaomiNFCTagData(
            deviceType = XiaomiDeviceType.PC,
            btMac = EMPTY,
            readOnly = false,
            enableLyra = true
        ),
        val defaultScreenMirrorData: XiaomiMirrorData = XiaomiMirrorData(
            deviceType = XiaomiDeviceType.PC,
            mirrorType = XiaomiMirrorType.FAKE_NFC_TAG,
            btMac = EMPTY,
            enableLyra = true
        ),
        val tilesMirrorData: XiaomiMirrorData = XiaomiMirrorData(
            deviceType = AppSettings.GlobalDefaults.tilesNfcDevice.toXiaomiDeviceType(),
            mirrorType = AppSettings.GlobalDefaults.tilesMirrorIntent.toXiaomiMirrorType(),
            btMac = EMPTY,
            enableLyra = AppSettings.GlobalDefaults.tilesEnableLyra
        ),
        val huaweiRedirectData: HuaweiRedirectData = HuaweiRedirectData(
            deviceType = AppSettings.GlobalDefaults.huaweiRedirectNfcDevice.toXiaomiDeviceType(),
            mirrorType = AppSettings.GlobalDefaults.huaweiRedirectMirrorIntent.toXiaomiMirrorType(),
            enableLyra = AppSettings.GlobalDefaults.huaweiRedirectEnableLyra
        ),
        val ndefWriteDialogData: NdefWriteData? = null,
        val showNotSupportedOSDialog: Boolean = false,
        val miLinkPackageDialogData: MiLinkVersionDialogData? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _snackbarMsg = MutableSharedFlow<SnackbarMsg>()
    val snackbarMsg: SharedFlow<SnackbarMsg> = _snackbarMsg.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            checkNotSupportedOS()
            checkLyraSupport()
            initDataStoreListeners()
        }
    }

    private suspend fun validateBtMac(btMac: String): Boolean {
        if (btMac.isBlank()) {
            _snackbarMsg.emit(SnackbarMsg.EMPTY_MAC_ADDRESS)
        } else if (!btMac.isValidMacAddress()) {
            _snackbarMsg.emit(SnackbarMsg.INVALID_MAC_ADDRESS)
        } else {
            return true
        }
        return false
    }

    private suspend fun initDataStoreListeners() = coroutineScope {
        launch {
            AppSettings.global.data.getTilesXiaomiMirrorData().collect { data ->
                _uiState.update { it.copy(tilesMirrorData = data) }
            }
        }
        launch {
            AppSettings.global.data.getHuaweiRedirectData().collect { data ->
                _uiState.update { it.copy(huaweiRedirectData = data) }
            }
        }
    }

    private suspend fun checkNotSupportedOS() {
        if (!isXiaomiHyperOS() && !AppSettings.global.data.first().confirmedNotSupportedOSAlert) {
            _uiState.update {
                it.copy(showNotSupportedOSDialog = true)
            }
        }
    }

    private fun checkLyraSupport() {
        if (!MiContinuityUtils.isLocalDeviceSupportLyra(AppContext)) {
            _uiState.update {
                it.copy(
                    defaultNFCTagData = it.defaultNFCTagData.copy(enableLyra = false),
                    defaultScreenMirrorData = it.defaultScreenMirrorData.copy(enableLyra = false)
                )
            }
        }
    }

    fun confirmNotSupportedOS() {
        viewModelScope.launch(Dispatchers.IO) {
            AppSettings.global.updateData {
                it.toBuilder().apply {
                    confirmedNotSupportedOSAlert = true
                }.build()
            }
            _uiState.update {
                it.copy(showNotSupportedOSDialog = false)
            }
        }
    }

    fun requestWriteNfc(nfcTagData: XiaomiNFCTagData) {
        viewModelScope.launch {
            if (validateBtMac(nfcTagData.btMac)) {
                val ndefMsg = XiaomiNfc.ScreenMirror.newNdefMessage(
                    XiaomiNfc.ScreenMirror.Config(nfcTagData.deviceType.nfcType, nfcTagData.btMac, nfcTagData.enableLyra)
                )
                val data = NdefWriteData(ndefMsg, nfcTagData.readOnly)
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

    fun requestClearNfc() {
        _uiState.update {
            it.copy(
                ndefWriteDialogData = NdefWriteData(
                    ndefMsg = EmptyNdefMessage,
                    readOnly = false
                )
            )
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

    fun sendScreenMirror(context: Context, mirrorData: XiaomiMirrorData) {
        viewModelScope.launch {
            if (validateBtMac(mirrorData.btMac)) {
                val nfcDeviceType = mirrorData.deviceType.nfcType
                val config = XiaomiNfc.ScreenMirror.Config(nfcDeviceType, mirrorData.btMac, mirrorData.enableLyra)
                when (mirrorData.mirrorType) {
                    XiaomiMirrorType.FAKE_NFC_TAG -> XiaomiNfc.ScreenMirror.newNdefDiscoveredIntent(null, null, config).also {
                        ContextCompat.startActivity(context, it, null)
                    }

                    XiaomiMirrorType.MI_CONNECT_SERVICE -> XiaomiNfc.ScreenMirror.sendBroadcast(context, config)
                }
            }
        }
    }

    fun requestAddTiles(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService<StatusBarManager>()?.requestAddTileService(
                ComponentName(context, MiShareTileService::class.java),
                context.getString(R.string.mi_share_tile_service),
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

    fun updateTilesMirrorData(mirrorData: XiaomiMirrorData) {
        _uiState.update {
            it.copy(tilesMirrorData = mirrorData)
        }
    }

    fun saveTilesMirrorData() {
        viewModelScope.launch(Dispatchers.IO) {
            val mirrorData = uiState.value.tilesMirrorData
            if (validateBtMac(mirrorData.btMac)) {
                AppSettings.global.updateData {
                    it.toBuilder().apply {
                        tilesNfcBtMac = mirrorData.btMac
                        tilesNfcDevice = mirrorData.deviceType.protoType
                        tilesMirrorIntent = mirrorData.mirrorType.protoType
                        tilesEnableLyra = BoolValue.of(mirrorData.enableLyra)
                    }.build()
                }
                _snackbarMsg.emit(SnackbarMsg.SAVE_SUCCESS)
            }
        }
    }

    fun updateHuaweiRedirectData(redirectData: HuaweiRedirectData) {
        _uiState.update {
            it.copy(huaweiRedirectData = redirectData)
        }
    }

    fun saveHuaweiRedirectData() {
        viewModelScope.launch(Dispatchers.IO) {
            val redirectData = uiState.value.huaweiRedirectData
            AppSettings.global.updateData {
                it.toBuilder().apply {
                    huaweiRedirectNfcDevice = redirectData.deviceType.protoType
                    huaweiRedirectMirrorIntent = redirectData.mirrorType.protoType
                    huaweiRedirectEnableLyra = BoolValue.of(redirectData.enableLyra)
                }.build()
            }
            _snackbarMsg.emit(SnackbarMsg.SAVE_SUCCESS)
        }
    }
}