package tool.xfy9326.milink.nfc.activity

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.ui.screen.MainScreen
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.vm.MainViewModel
import tool.xfy9326.milink.nfc.utils.enableNdefReaderMode
import tool.xfy9326.milink.nfc.utils.ignoreTagUntilRemoved
import tool.xfy9326.milink.nfc.utils.showToast
import tool.xfy9326.milink.nfc.utils.tryConnect
import tool.xfy9326.milink.nfc.utils.useCatching

class MainActivity : ComponentActivity() {
    private val nfcAdapter by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainScreen {
                    finishAndRemoveTask()
                }
            }
        }
        setupNfcReaderListener()
    }

    private fun setupNfcReaderListener() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.nfcWriteData.collect { data ->
                    data?.let(::openNdefReader) ?: closeNdefReader()
                }
            }
        }
    }

    private suspend fun makeToast(msg: String): Unit =
        withContext(Dispatchers.Main.immediate) {
            showToast(msg)
        }

    private fun openNdefReader(writeData: NdefWriteData) {
        nfcAdapter.enableNdefReaderMode(this) {
            handleNfcTag(it, writeData)
        }
    }

    private fun handleNfcTag(tag: Tag, writeData: NdefWriteData) {
        lifecycleScope.launch {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                writeNdefTag(ndef, writeData)
                return@launch
            }
            val ndefFormatable = NdefFormatable.get(tag)
            if (ndefFormatable != null) {
                formatNdefTag(ndefFormatable)
                return@launch
            }
            makeToast(getString(R.string.nfc_ndef_not_supported))
        }
    }

    private suspend fun formatNdefTag(ndefFormatable: NdefFormatable): Unit = withContext(Dispatchers.IO) {
        ndefFormatable.tryConnect().onSuccess {
            it.useCatching {
                format(null)
            }.onSuccess {
                nfcAdapter.ignoreTagUntilRemoved(ndefFormatable.tag)
                makeToast(getString(R.string.nfc_ndef_format_success))
            }.onFailure {
                makeToast(getString(R.string.nfc_ndef_format_failed))
            }
        }.onFailure {
            makeToast(getString(R.string.nfc_connect_failed))
        }
    }

    private suspend fun writeNdefTag(ndef: Ndef, writeData: NdefWriteData): Unit = withContext(Dispatchers.IO) {
        ndef.tryConnect().onSuccess {
            it.useCatching {
                if (!it.isWritable) {
                    makeToast(getString(R.string.nfc_write_error_not_writeable))
                    return@onSuccess
                }
                if (writeData.readOnly && !it.canMakeReadOnly()) {
                    makeToast(getString(R.string.nfc_write_error_no_read_only))
                    return@onSuccess
                }
                if (writeData.ndefMsg.byteArrayLength > it.maxSize) {
                    makeToast(getString(R.string.nfc_write_error_max_size))
                    return@onSuccess
                }
                try {
                    it.writeNdefMessage(writeData.ndefMsg)
                } catch (e: Exception) {
                    makeToast(getString(R.string.nfc_write_error))
                    return@onSuccess
                }
                if (writeData.readOnly) {
                    try {
                        if (!it.makeReadOnly()) {
                            makeToast(getString(R.string.nfc_write_error_read_only))
                            return@onSuccess
                        }
                    } catch (e: Exception) {
                        makeToast(getString(R.string.nfc_write_error_read_only))
                        return@onSuccess
                    }
                }
            }.onSuccess {
                makeToast(getString(R.string.nfc_write_success))
                nfcAdapter.ignoreTagUntilRemoved(ndef.tag)
                if (writeData.readOnly) viewModel.closeNfcWriteDialog()
            }.onFailure { throwable ->
                makeToast(
                    buildString {
                        append(getString(R.string.nfc_write_error_unknown))
                        throwable.message.takeUnless { m -> m.isNullOrBlank() }?.let { m ->
                            appendLine()
                            append(m)
                        }
                    }
                )
            }
        }.onFailure {
            makeToast(getString(R.string.nfc_connect_failed))
        }
    }

    private fun closeNdefReader() {
        nfcAdapter.disableReaderMode(this)
    }
}
