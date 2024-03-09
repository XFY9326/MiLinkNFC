package tool.xfy9326.milink.nfc.activity

import android.content.Intent
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
import tool.xfy9326.milink.nfc.utils.enableNdefForegroundDispatch
import tool.xfy9326.milink.nfc.utils.ignoreTagUntilRemoved
import tool.xfy9326.milink.nfc.utils.isNullOrEmpty
import tool.xfy9326.milink.nfc.utils.requireConnect
import tool.xfy9326.milink.nfc.utils.requireEnabled
import tool.xfy9326.milink.nfc.utils.safeClose
import tool.xfy9326.milink.nfc.utils.showToast
import tool.xfy9326.milink.nfc.utils.showToastInMain
import tool.xfy9326.milink.nfc.utils.techNameList
import tool.xfy9326.milink.nfc.utils.tryGetNfcTag

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
        NfcAdapter.getDefaultAdapter(this)?.also { adapter ->
            if (adapter.requireEnabled()) {
                adapter.enableNdefForegroundDispatch(this)
            } else {
                showToast(R.string.nfc_disabled)
            }
        }
    }

    override fun onPause() {
        NfcAdapter.getDefaultAdapter(this)?.disableForegroundDispatch(this)
        super.onPause()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.tryGetNfcTag()?.also(::onNfcTagDiscovered)
    }

    private fun onNfcTagDiscovered(tag: Tag) {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            lifecycleScope.launch {
                ndef.readNdef()
                ignoreTagUntilRemoved(tag)
            }
        } else {
            if (NdefFormatable.get(tag) != null) {
                showToast(R.string.nfc_ndef_formatable)
            } else {
                showToast(R.string.nfc_not_ndef)
            }
            viewModel.clearNfcReadData()
        }
    }

    private suspend fun Ndef.readNdef(): Unit = withContext(Dispatchers.IO) {
        if (requireConnect()) {
            try {
                val msg = ndefMessage
                if (msg.isNullOrEmpty()) {
                    showToastInMain(R.string.nfc_empty)
                } else {
                    NdefReadData(
                        scanTime = System.currentTimeMillis(),
                        techList = tag.techNameList,
                        type = type,
                        msg = msg,
                        maxSize = maxSize,
                        writeable = isWritable,
                        canMakeReadOnly = canMakeReadOnly()
                    ).also { data ->
                        viewModel.updateNfcReadData(data)
                    }
                }
                safeClose()
            } catch (e: Exception) {
                showToastInMain(R.string.nfc_read_failed)
                viewModel.clearNfcReadData()
            }
        } else {
            showToastInMain(R.string.nfc_connect_failed)
            viewModel.clearNfcReadData()
        }
    }
}