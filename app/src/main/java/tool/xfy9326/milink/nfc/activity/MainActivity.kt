package tool.xfy9326.milink.nfc.activity

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.ui.screen.MainScreen
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.vm.MainViewModel
import tool.xfy9326.milink.nfc.utils.enableNdefReaderMode
import tool.xfy9326.milink.nfc.utils.ignoreTagUntilRemoved
import tool.xfy9326.milink.nfc.utils.showToast

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

    private fun showToastOnUiThread(msg: String) {
        runOnUiThread { showToast(msg) }
    }

    private fun openNdefReader(writeData: NdefWriteData) {
        nfcAdapter.enableNdefReaderMode(this) { tag ->
            writeNfcTag(tag, writeData)
        }
    }

    private fun writeNfcTag(tag: Tag, writeData: NdefWriteData) {
        try {
            Ndef.get(tag)?.use {
                try {
                    it.connect()
                    require(it.isConnected)
                } catch (e: Exception) {
                    showToastOnUiThread(getString(R.string.nfc_connect_failed))
                    return@use
                }
                if (!it.isWritable) {
                    showToastOnUiThread(getString(R.string.nfc_write_error_not_writeable))
                    return@use
                }
                if (writeData.readOnly && !it.canMakeReadOnly()) {
                    showToastOnUiThread(getString(R.string.nfc_write_error_no_read_only))
                    return@use
                }
                if (writeData.ndefMsg.byteArrayLength > it.maxSize) {
                    showToastOnUiThread(getString(R.string.nfc_write_error_max_size))
                    return@use
                }
                try {
                    it.writeNdefMessage(writeData.ndefMsg)
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToastOnUiThread(getString(R.string.nfc_write_error))
                    return@use
                }
                if (writeData.readOnly) {
                    try {
                        if (!it.makeReadOnly()) {
                            showToastOnUiThread(getString(R.string.nfc_write_error_read_only))
                            return@use
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showToastOnUiThread(getString(R.string.nfc_write_error))
                        return@use
                    }
                }
                nfcAdapter.ignoreTagUntilRemoved(tag)
                if (writeData.readOnly) {
                    viewModel.closeNfcWriteDialog()
                }
                showToastOnUiThread(getString(R.string.nfc_write_success))
            } ?: showToastOnUiThread(getString(R.string.nfc_ndef_not_supported))
        } catch (e: Exception) {
            e.printStackTrace()
            showToastOnUiThread(
                buildString {
                    append(getString(R.string.nfc_write_error_unknown))
                    e.message?.let {
                        appendLine()
                        append(it)
                    }
                }
            )
        }
    }

    private fun closeNdefReader() {
        nfcAdapter.disableReaderMode(this)
    }
}
