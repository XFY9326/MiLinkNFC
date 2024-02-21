package tool.xfy9326.milink.nfc.activity

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.ui.screen.HomeScreen
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.vm.MainViewModel
import tool.xfy9326.milink.nfc.utils.EmptyNdefMessage
import tool.xfy9326.milink.nfc.utils.MIME_ALL
import tool.xfy9326.milink.nfc.utils.enableNdefReaderMode
import tool.xfy9326.milink.nfc.utils.ignoreTagUntilRemoved
import tool.xfy9326.milink.nfc.utils.showToast
import tool.xfy9326.milink.nfc.utils.startActivity
import tool.xfy9326.milink.nfc.utils.tryConnect

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private val readNdefBin = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            viewModel.requestNdefBinWriteDialog(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                HomeScreen(
                    onNavToXiaomiNfcReader = { startReaderActivity() },
                    onRequestWriteNdefBin = { readNdefBin.launch(MIME_ALL) },
                    onExit = { finishAndRemoveTask() }
                )
            }
        }
    }

    private fun startReaderActivity() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            showToast(getString(R.string.not_support_nfc))
        } else if (!nfcAdapter.isEnabled) {
            showToast(getString(R.string.nfc_disabled))
        } else {
            startActivity<XiaomiNfcReaderActivity>()
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            viewModel.instantMsg.collect {
                showToast(getString(it.resId))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.nfcWriteData.collect { data ->
                NfcAdapter.getDefaultAdapter(this@MainActivity)?.apply {
                    data?.let {
                        enableNdefReaderMode(this@MainActivity) {
                            handleNfcTag(this, it, data)
                        }
                    } ?: disableReaderMode(this@MainActivity)
                }
            }
        }
    }

    private fun makeToast(msg: String): Unit = runOnUiThread { showToast(msg) }

    private fun handleNfcTag(nfcAdapter: NfcAdapter, tag: Tag, writeData: NdefWriteData) {
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
            makeToast(
                getString(
                    R.string.nfc_ndef_not_supported,
                    tag.techList.joinToString { it.substringAfterLast(".") })
            )
        }
    }

    private suspend fun formatNdefTag(nfcAdapter: NfcAdapter, ndefFormatable: NdefFormatable): Unit = withContext(Dispatchers.IO) {
        ndefFormatable.tryConnect().onSuccess {
            it.runCatching {
                use { format(null) }
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

    private suspend fun writeNdefTag(nfcAdapter: NfcAdapter, ndef: Ndef, writeData: NdefWriteData): Unit = withContext(Dispatchers.IO) {
        ndef.tryConnect().onSuccess {
            it.runCatching {
                use {
                    safeWriteNdefData(writeData) { resId -> makeToast(getString(resId)) }
                }
            }.onSuccess {
                makeToast(getString(R.string.nfc_write_success))
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
            nfcAdapter.ignoreTagUntilRemoved(ndef.tag)
        }.onFailure {
            makeToast(getString(R.string.nfc_connect_failed))
        }
    }

    private fun Ndef.safeWriteNdefData(writeData: NdefWriteData, onErrorResMsg: (Int) -> Unit) {
        if (!isWritable) {
            onErrorResMsg(R.string.nfc_write_error_not_writeable)
            return
        }
        if (writeData.readOnly && !canMakeReadOnly()) {
            onErrorResMsg(R.string.nfc_write_error_no_read_only)
            return
        }

        val ndefMsg = writeData.msg?.let { msg ->
            if (msg.byteArrayLength > maxSize) {
                onErrorResMsg(R.string.nfc_write_error_max_size)
                return
            }
            msg
        } ?: EmptyNdefMessage

        try {
            writeNdefMessage(ndefMsg)
        } catch (e: Exception) {
            onErrorResMsg(R.string.nfc_write_error)
            return
        }
        if (writeData.readOnly) {
            try {
                if (!makeReadOnly()) {
                    onErrorResMsg(R.string.nfc_write_error_read_only)
                    return
                }
            } catch (e: Exception) {
                onErrorResMsg(R.string.nfc_write_error_read_only)
                return
            }
        }
    }
}
