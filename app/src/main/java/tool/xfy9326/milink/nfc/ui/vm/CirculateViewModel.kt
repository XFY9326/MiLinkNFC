package tool.xfy9326.milink.nfc.ui.vm

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.Circulate
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.data.NfcActionIntentType
import tool.xfy9326.milink.nfc.datastore.AppDataStore
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc
import tool.xfy9326.milink.nfc.utils.EMPTY
import tool.xfy9326.milink.nfc.utils.hexToByteArray
import tool.xfy9326.milink.nfc.utils.isValidMacAddress
import tool.xfy9326.milink.nfc.utils.safeStartActivity

class CirculateViewModel : ViewModel() {
    enum class InstantMsg(@StringRes val resId: Int) {
        INVALID_MAC_ADDRESS(R.string.invalid_mac_address),
        EMPTY_MAC_ADDRESS(R.string.empty_mac_address),
    }

    @Parcelize
    data class NFCTag(
        val deviceType: Circulate.DeviceType = Circulate.DeviceType.SOUND_BOX,
        val wifiMac: String = EMPTY,
        val bluetoothMac: String = EMPTY,
        val readOnly: Boolean = false
    ) : Parcelable {
        fun toConfig(writeTime: Int) =
            XiaomiNfc.Circulate.Config(
                writeTime = writeTime,
                deviceType = deviceType.nfcTagDeviceType,
                wifiMac = wifiMac.hexToByteArray(true),
                bluetoothMac = bluetoothMac.hexToByteArray(true)
            )
    }

    data class UiState(
        val nfcTag: NFCTag = NFCTag(),
        val testCirculate: Circulate = Circulate(
            deviceType = Circulate.DeviceType.SOUND_BOX,
            actionIntentType = NfcActionIntentType.FAKE_NFC_TAG,
            wifiMac = EMPTY,
            bluetoothMac = EMPTY
        )
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _instantMsg = MutableSharedFlow<InstantMsg>()
    val instantMsg: SharedFlow<InstantMsg> = _instantMsg.asSharedFlow()

    private suspend fun validateMacAddress(macAddress: String): Boolean {
        if (macAddress.isBlank()) {
            _instantMsg.emit(InstantMsg.EMPTY_MAC_ADDRESS)
        } else if (!macAddress.isValidMacAddress()) {
            _instantMsg.emit(InstantMsg.INVALID_MAC_ADDRESS)
        } else {
            return true
        }
        return false
    }

    fun requestWriteNfc(nfcTagData: NFCTag, ndefWriteHandler: (NdefWriteData) -> Unit) {
        viewModelScope.launch {
            if (validateMacAddress(nfcTagData.wifiMac) && validateMacAddress(nfcTagData.bluetoothMac)) {
                val writeTime = (System.currentTimeMillis() / 1000).toInt()
                val ndefMsg = XiaomiNfc.Circulate.newNdefMessage(
                    nfcTagData.toConfig(writeTime),
                    AppDataStore.shrinkNdefMsg.getValue()
                )
                val data = NdefWriteData(ndefMsg, nfcTagData.readOnly)
                ndefWriteHandler(data)
            }
        }
    }

    fun sendCirculate(context: Context, circulate: Circulate) {
        viewModelScope.launch {
            if (validateMacAddress(circulate.wifiMac) && validateMacAddress(circulate.bluetoothMac)) {
                val config = circulate.toConfig((System.currentTimeMillis() / 1000).toInt())
                when (circulate.actionIntentType) {
                    NfcActionIntentType.FAKE_NFC_TAG -> XiaomiNfc.Circulate.newNdefDiscoveredIntent(
                        null,
                        null,
                        config
                    ).also {
                        context.safeStartActivity(it)
                    }

                    NfcActionIntentType.MI_CONNECT_SERVICE -> XiaomiNfc.Circulate.sendBroadcast(
                        context,
                        config
                    )
                }
            }
        }
    }
}