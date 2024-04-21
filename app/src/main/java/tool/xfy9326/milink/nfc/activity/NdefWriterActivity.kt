package tool.xfy9326.milink.nfc.activity

import android.content.Context
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
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.ui.screen.NdefWriterScreen
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.vm.NdefWriterViewModel
import tool.xfy9326.milink.nfc.utils.MIME_ALL
import tool.xfy9326.milink.nfc.utils.enableNdefForegroundDispatch
import tool.xfy9326.milink.nfc.utils.ignoreTagUntilRemoved
import tool.xfy9326.milink.nfc.utils.requireConnect
import tool.xfy9326.milink.nfc.utils.requireEnabled
import tool.xfy9326.milink.nfc.utils.safeClose
import tool.xfy9326.milink.nfc.utils.showToast
import tool.xfy9326.milink.nfc.utils.showToastInMain
import tool.xfy9326.milink.nfc.utils.startActivity
import tool.xfy9326.milink.nfc.utils.techNameList
import tool.xfy9326.milink.nfc.utils.tryGetNfcTag

class NdefWriterActivity : ComponentActivity() {
    companion object {
        private const val EXTRA_NDEF_DATA = "EXTRA_NDEF_DATA"

        fun openActivity(context: Context, ndefWriteData: NdefWriteData) {
            context.startActivity<NdefWriterActivity> {
                putExtra(EXTRA_NDEF_DATA, ndefWriteData)
            }
        }
    }

    private lateinit var ndefWriteData: NdefWriteData
    private val viewModel by viewModels<NdefWriterViewModel>()
    private val exportNdefBin = registerForActivityResult(ActivityResultContracts.CreateDocument(MIME_ALL)) {
        if (it != null) {
            viewModel.exportNdefBin(it, ndefWriteData.msg.toByteArray())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ndefWriteData = IntentCompat.getParcelableExtra(intent, EXTRA_NDEF_DATA, NdefWriteData::class.java) ?: error("Unknown intent")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                NdefWriterScreen(
                    ndefWriteData = ndefWriteData,
                    onNavBack = onBackPressedDispatcher::onBackPressed
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
                adapter.enableNdefForegroundDispatch(this, true)
            } else {
                showToast(R.string.nfc_disabled)
            }
        }
    }

    override fun onPause() {
        NfcAdapter.getDefaultAdapter(this)?.disableForegroundDispatch(this)
        viewModel.setWritingStatus(false)
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.tryGetNfcTag()?.also(::onNfcTagDiscovered)
    }

    private fun onNfcTagDiscovered(tag: Tag) {
        viewModel.setWritingStatus(true)
        lifecycleScope.launch {
            try {
                val ndef = Ndef.get(tag)
                if (ndef != null) {
                    ndef.writeTag()
                    return@launch
                }
                val ndefFormatable = NdefFormatable.get(tag)
                if (ndefFormatable != null) {
                    ndefFormatable.formatTag()
                    return@launch
                }
                showToast(R.string.nfc_ndef_not_supported, tag.techNameList.joinToString())
            } finally {
                ignoreTagUntilRemoved(tag)
                viewModel.setWritingStatus(false)
            }
        }
    }

    private suspend fun NdefFormatable.formatTag(): Unit = withContext(Dispatchers.IO) {
        if (requireConnect()) {
            try {
                format(null)
                showToastInMain(R.string.nfc_ndef_format_success)
            } catch (e: Exception) {
                showToastInMain(R.string.nfc_ndef_format_failed)
            }
            if (!safeClose()) {
                showToastInMain(R.string.nfc_close_failed)
            }
        } else {
            showToastInMain(R.string.nfc_connect_failed)
        }
    }

    private suspend fun Ndef.writeTag(): Unit = withContext(Dispatchers.IO) {
        if (requireConnect()) {
            try {
                if (writeNdefData()) {
                    showToastInMain(R.string.nfc_write_success)
                }
            } catch (e: Exception) {
                showToastInMain(R.string.nfc_write_failed)
            }
            if (!safeClose()) {
                showToastInMain(R.string.nfc_close_failed)
            }
        } else {
            showToastInMain(R.string.nfc_connect_failed)
        }
    }

    private suspend fun Ndef.writeNdefData(): Boolean {
        if (!isWritable) {
            showToastInMain(R.string.nfc_write_error_not_writeable)
            return false
        }
        if (ndefWriteData.readOnly && !canMakeReadOnly()) {
            showToastInMain(R.string.nfc_write_error_no_read_only)
            return false
        }
        if (ndefWriteData.msg.byteArrayLength > maxSize) {
            showToastInMain(R.string.nfc_write_error_max_size)
            return false
        }
        try {
            writeNdefMessage(ndefWriteData.msg)
        } catch (e: Exception) {
            showToastInMain(R.string.nfc_write_error)
            return false
        }
        if (ndefWriteData.readOnly) {
            try {
                if (!makeReadOnly()) {
                    showToastInMain(R.string.nfc_write_error_read_only)
                    return false
                }
            } catch (e: Exception) {
                showToastInMain(R.string.nfc_write_error_read_only)
                return false
            }
        }
        return true
    }
}