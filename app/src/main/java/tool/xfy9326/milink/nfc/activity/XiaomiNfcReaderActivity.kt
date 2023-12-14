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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefReadData
import tool.xfy9326.milink.nfc.ui.screen.XiaomiNfcReaderScreen
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.vm.XiaomiNfcReaderViewModel
import tool.xfy9326.milink.nfc.utils.MIME_ALL
import tool.xfy9326.milink.nfc.utils.MIME_BINARY
import tool.xfy9326.milink.nfc.utils.enableNdefReaderMode
import tool.xfy9326.milink.nfc.utils.ignoreTagUntilRemoved
import tool.xfy9326.milink.nfc.utils.isNullOrEmpty
import tool.xfy9326.milink.nfc.utils.showToast
import tool.xfy9326.milink.nfc.utils.tryConnect

class XiaomiNfcReaderActivity : ComponentActivity() {
    private val viewModel by viewModels<XiaomiNfcReaderViewModel>()
    private val exportNdefBin = registerForActivityResult(ActivityResultContracts.CreateDocument(MIME_BINARY)) {
        if (it == null) {
            showToast(getString(R.string.export_canceled))
        } else {
            viewModel.exportNdefBin(it)
        }
    }
    private val importNdefBin = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) {
            showToast(getString(R.string.import_canceled))
        } else {
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
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.exportNdefBin.collect {
                    exportNdefBin.launch(it)
                }
            }
        }
    }

    private fun makeToast(msg: String): Unit = runOnUiThread { showToast(msg) }

    override fun onResume() {
        super.onResume()
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter?.let { adapter ->
            adapter.enableNdefReaderMode(this) {
                val ndef = Ndef.get(it)
                if (ndef != null) {
                    lifecycleScope.launch {
                        readNdef(adapter, ndef)
                    }
                    return@enableNdefReaderMode
                }
                val ndefFormatable = NdefFormatable.get(it)
                if (ndefFormatable != null) {
                    makeToast(getString(R.string.nfc_ndef_formatable))
                } else {
                    makeToast(getString(R.string.nfc_not_ndef))
                }
                viewModel.clearNfcReadData()
            }
        }
    }

    override fun onPause() {
        NfcAdapter.getDefaultAdapter(this)?.disableReaderMode(this)
        super.onPause()
    }

    private suspend fun readNdef(nfcAdapter: NfcAdapter, ndef: Ndef) = withContext(Dispatchers.IO) {
        ndef.tryConnect().onSuccess {
            try {
                val msg = it.ndefMessage
                if (msg.isNullOrEmpty()) {
                    makeToast(getString(R.string.nfc_empty))
                } else {
                    NdefReadData(
                        scanTime = System.currentTimeMillis(),
                        techList = it.tag.techList.map { str -> str.substringAfterLast(".") },
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
                makeToast(getString(R.string.nfc_read_failed))
                viewModel.clearNfcReadData()
            }
            nfcAdapter.ignoreTagUntilRemoved(ndef.tag)
        }.onFailure {
            makeToast(getString(R.string.nfc_connect_failed))
            viewModel.clearNfcReadData()
        }
    }
}