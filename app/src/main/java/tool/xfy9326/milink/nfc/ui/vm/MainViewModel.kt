package tool.xfy9326.milink.nfc.ui.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Semaphore
import tool.xfy9326.milink.nfc.data.NdefData

class MainViewModel : ViewModel() {
    companion object {
        private const val PERMITS_NFC_USING = 1
    }

    private val nfcUsing = Semaphore(PERMITS_NFC_USING)

    private val _nfcWriteData = MutableStateFlow<NdefData?>(null)
    val nfcWriteData: StateFlow<NdefData?> = _nfcWriteData.asStateFlow()

    fun openNFCWriter(ndefData: NdefData): Boolean {
        return if (nfcUsing.tryAcquire()) {
            _nfcWriteData.update { ndefData }
            true
        } else {
            false
        }
    }

    fun closeNfcWriter() {
        _nfcWriteData.update { null }
        try {
            if (nfcUsing.availablePermits < PERMITS_NFC_USING) {
                nfcUsing.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}