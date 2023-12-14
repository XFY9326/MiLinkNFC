package tool.xfy9326.milink.nfc.ui.vm

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
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.datastore.AppDataStore
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc
import tool.xfy9326.milink.nfc.utils.EMPTY
import tool.xfy9326.milink.nfc.utils.hexToByteArray
import tool.xfy9326.milink.nfc.utils.isValidMacAddress

class MiTapSoundBoxViewModel : ViewModel() {
    enum class InstantMsg(@StringRes val resId: Int) {
        INVALID_MAC_ADDRESS(R.string.invalid_mac_address),
        EMPTY_MAC_ADDRESS(R.string.empty_mac_address),
    }

    @Parcelize
    data class NFCTag(
        val wifiMac: String = EMPTY,
        val bluetoothMac: String = EMPTY,
        val model: String = EMPTY,
        val readOnly: Boolean = false
    ) : Parcelable {
        fun toConfig(writeTime: Int) =
            XiaomiNfc.MiTapSoundBox.Config(
                writeTime = writeTime,
                wifiMac = wifiMac.takeIf { it.isNotBlank() }?.hexToByteArray(true),
                bluetoothMac = bluetoothMac.hexToByteArray(true),
                model = model.takeIf { it.isNotBlank() }
            )
    }

    data class UiState(
        val nfcTag: NFCTag = NFCTag()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _instantMsg = MutableSharedFlow<InstantMsg>()
    val instantMsg: SharedFlow<InstantMsg> = _instantMsg.asSharedFlow()

    private suspend fun validateMacAddress(macAddress: String, allowEmpty: Boolean): Boolean {
        if (macAddress.isBlank()) {
            if (allowEmpty) {
                return true
            } else {
                _instantMsg.emit(InstantMsg.EMPTY_MAC_ADDRESS)
            }
        } else if (!macAddress.isValidMacAddress()) {
            _instantMsg.emit(InstantMsg.INVALID_MAC_ADDRESS)
        } else {
            return true
        }
        return false
    }

    fun requestWriteNfc(nfcTagData: NFCTag, ndefWriteHandler: (NdefWriteData) -> Unit) {
        viewModelScope.launch {
            if (validateMacAddress(nfcTagData.wifiMac, true) && validateMacAddress(nfcTagData.bluetoothMac, false)) {
                val writeTime = (System.currentTimeMillis() / 1000).toInt()
                val ndefMsg = XiaomiNfc.MiTapSoundBox.newNdefMessage(nfcTagData.toConfig(writeTime), AppDataStore.shrinkNdefMsg.getValue())
                val data = NdefWriteData(ndefMsg, nfcTagData.readOnly)
                ndefWriteHandler(data)
            }
        }
    }
}