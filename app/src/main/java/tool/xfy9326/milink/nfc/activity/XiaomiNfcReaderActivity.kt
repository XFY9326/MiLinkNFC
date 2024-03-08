package tool.xfy9326.milink.nfc.activity

import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefReadData
import tool.xfy9326.milink.nfc.ui.screen.XiaomiNfcReaderScreen
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.vm.XiaomiNfcReaderViewModel
import tool.xfy9326.milink.nfc.utils.MIME_ALL
import tool.xfy9326.milink.nfc.utils.enableNdefReaderMode
import tool.xfy9326.milink.nfc.utils.ignoreTagUntilRemoved
import tool.xfy9326.milink.nfc.utils.isNullOrEmpty
import tool.xfy9326.milink.nfc.utils.safeClose
import tool.xfy9326.milink.nfc.utils.showToast
import tool.xfy9326.milink.nfc.utils.techNameList
import tool.xfy9326.milink.nfc.utils.tryConnect

class XiaomiNfcReaderActivity : ComponentActivity() {
    private val viewModel by viewModels<XiaomiNfcReaderViewModel>()
    private val exportNdefBin = registerForActivityResult(ActivityResultContracts.CreateDocument(MIME_ALL)) {
        if (it != null) {
            viewModel.exportNdefBin(it)
        }
    }
    private val importNdefBin = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            viewModel.updateNfcReadData(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                XiaomiNfcReaderScreen(
                    onNavBack = {
                        onBackPressedDispatcher.onBackPressed()
                    },
                    onRequestImportNdefBin = {
                        importNdefBin.launch(MIME_ALL)
                    }
                )
            }
        }
        lifecycleScope.launch {
            viewModel.exportNdefBin.collect {
                exportNdefBin.launch(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        NfcAdapter.getDefaultAdapter(this)?.let { adapter ->
            if (adapter.isEnabled) {
                adapter.enableNdefReaderMode(this) {
                    val ndef = Ndef.get(it)
                    if (ndef != null) {
                        lifecycleScope.launch { ndef.readNdef(adapter) }
                    } else {
                        if (NdefFormatable.get(it) != null) {
                            showToast(R.string.nfc_ndef_formatable)
                        } else {
                            showToast(R.string.nfc_not_ndef)
                        }
                        viewModel.clearNfcReadData()
                    }
                }
            } else {
                showToast(R.string.nfc_disabled)
            }
        }
    }

    override fun onPause() {
        NfcAdapter.getDefaultAdapter(this)?.disableReaderMode(this)
        super.onPause()
    }

    private suspend fun Ndef.readNdef(adapter: NfcAdapter) {
        lifecycleScope.launch(Dispatchers.Main.immediate) {
            tryConnect().onSuccess {
                try {
                    val msg = withContext(Dispatchers.IO) { it.ndefMessage }
                    if (msg.isNullOrEmpty()) {
                        showToast(R.string.nfc_empty)
                    } else {
                        NdefReadData(
                            scanTime = System.currentTimeMillis(),
                            techList = it.tag.techNameList,
                            type = it.type,
                            msg = msg,
                            maxSize = it.maxSize,
                            writeable = it.isWritable,
                            canMakeReadOnly = it.canMakeReadOnly()
                        ).also { data ->
                            viewModel.updateNfcReadData(data)
                        }
                    }
                } catch (e: Exception) {
                    showToast(R.string.nfc_read_failed)
                    viewModel.clearNfcReadData()
                }
            }.onFailure {
                showToast(R.string.nfc_connect_failed)
                viewModel.clearNfcReadData()
            }
        }
        safeClose()
        adapter.ignoreTagUntilRemoved(tag)
    }
}