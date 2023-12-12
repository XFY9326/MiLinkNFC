package tool.xfy9326.milink.nfc.activity

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.widget.Toast
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
import tool.xfy9326.milink.nfc.AppContext
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefData
import tool.xfy9326.milink.nfc.ui.screen.NFCMirrorScreen
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.vm.MainViewModel
import tool.xfy9326.milink.nfc.utils.enableNdefReaderMode
import tool.xfy9326.milink.nfc.utils.ignoreTagUntilRemoved
import tool.xfy9326.milink.nfc.utils.showToast
import tool.xfy9326.milink.nfc.utils.tryConnect
import tool.xfy9326.milink.nfc.utils.useCatching

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                NFCMirrorScreen(onExit = { finishAndRemoveTask() })
            }
        }
        setupNfcReaderListener()
    }

    private fun setupNfcReaderListener() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter != null) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.nfcWriteData.collect { data ->
                        data?.let {
                            nfcAdapter.enableNdefReaderMode(this@MainActivity) {
                                handleNfcTag(nfcAdapter, it, data)
                            }
                        } ?: nfcAdapter.disableReaderMode(this@MainActivity)
                    }
                }
            }
        } else {
            Toast.makeText(AppContext, R.string.no_nfc, Toast.LENGTH_LONG).show()
        }
    }

    private fun makeToast(msg: String): Unit = runOnUiThread { showToast(msg) }

    private fun handleNfcTag(nfcAdapter: NfcAdapter, tag: Tag, writeData: NdefData) {
        lifecycleScope.launch {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                writeNdefTag(nfcAdapter, ndef, writeData)
                return@launch
            }
            val ndefFormatable = NdefFormatable.get(tag)
            if (ndefFormatable != null) {
                formatNdefTag(nfcAdapter, ndefFormatable)
                return@launch
            }
            makeToast(getString(R.string.nfc_ndef_not_supported, tag.techList.joinToString { it.substringAfterLast(".") }))
        }
    }

    private suspend fun formatNdefTag(nfcAdapter: NfcAdapter, ndefFormatable: NdefFormatable): Unit = withContext(Dispatchers.IO) {
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

    private suspend fun writeNdefTag(nfcAdapter: NfcAdapter, ndef: Ndef, writeData: NdefData): Unit = withContext(Dispatchers.IO) {
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
                if (writeData.msg.byteArrayLength > it.maxSize) {
                    makeToast(getString(R.string.nfc_write_error_max_size))
                    return@onSuccess
                }
                try {
                    it.writeNdefMessage(writeData.msg)
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
                if (writeData.readOnly) viewModel.closeNfcWriter()
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
}
