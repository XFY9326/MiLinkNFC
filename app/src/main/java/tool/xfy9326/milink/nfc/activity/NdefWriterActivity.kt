package tool.xfy9326.milink.nfc.activity

import android.content.Context
import android.nfc.NfcAdapter
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
import tool.xfy9326.milink.nfc.ui.vm.NfcWriterViewModel
import tool.xfy9326.milink.nfc.utils.MIME_ALL
import tool.xfy9326.milink.nfc.utils.enableNdefReaderMode
import tool.xfy9326.milink.nfc.utils.ignoreTagUntilRemoved
import tool.xfy9326.milink.nfc.utils.safeClose
import tool.xfy9326.milink.nfc.utils.showToast
import tool.xfy9326.milink.nfc.utils.startActivity
import tool.xfy9326.milink.nfc.utils.techNameList
import tool.xfy9326.milink.nfc.utils.tryConnect

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
    private val viewModel by viewModels<NfcWriterViewModel>()
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
        NfcAdapter.getDefaultAdapter(this)?.let { adapter ->
            if (adapter.isEnabled) {
                adapter.enableNdefReaderMode(this) {
                    lifecycleScope.launch(Dispatchers.Main.immediate) {
                        val ndef = Ndef.get(it)
                        if (ndef != null) {
                            ndef.writeTag(adapter)
                            return@launch
                        }
                        val ndefFormatable = NdefFormatable.get(it)
                        if (ndefFormatable != null) {
                            ndefFormatable.formatTag(adapter)
                            return@launch
                        }
                        showToast(R.string.nfc_ndef_not_supported, it.techNameList.joinToString())
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

    private suspend fun NdefFormatable.formatTag(adapter: NfcAdapter) {
        tryConnect().onSuccess {
            it.runCatching {
                format(null)
            }.onSuccess {
                showToast(R.string.nfc_ndef_format_success)
            }.onFailure {
                showToast(R.string.nfc_ndef_format_failed)
            }
        }.onFailure {
            showToast(R.string.nfc_connect_failed)
        }
        safeClose()
        adapter.ignoreTagUntilRemoved(tag)
    }

    private suspend fun Ndef.writeTag(adapter: NfcAdapter) {
        tryConnect().onSuccess {
            it.runCatching {
                writeNdefData()
            }.onSuccess {
                showToast(R.string.nfc_write_success)
                if (ndefWriteData.readOnly) onBackPressedDispatcher.onBackPressed()
            }.onFailure { throwable ->
                showToast(
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
            showToast(R.string.nfc_connect_failed)
        }
        safeClose()
        adapter.ignoreTagUntilRemoved(tag)
    }

    private suspend fun Ndef.writeNdefData() {
        if (!isWritable) {
            showToast(R.string.nfc_write_error_not_writeable)
            return
        }
        if (ndefWriteData.readOnly && !canMakeReadOnly()) {
            showToast(R.string.nfc_write_error_no_read_only)
            return
        }
        if (ndefWriteData.msg.byteArrayLength > maxSize) {
            showToast(R.string.nfc_write_error_max_size)
            return
        }
        try {
            withContext(Dispatchers.IO) {
                writeNdefMessage(ndefWriteData.msg)
            }
        } catch (e: Exception) {
            showToast(R.string.nfc_write_error)
            return
        }
        if (ndefWriteData.readOnly) {
            try {
                if (!withContext(Dispatchers.IO) { makeReadOnly() }) {
                    showToast(R.string.nfc_write_error_read_only)
                    return
                }
            } catch (e: Exception) {
                showToast(R.string.nfc_write_error_read_only)
                return
            }
        }
    }
}