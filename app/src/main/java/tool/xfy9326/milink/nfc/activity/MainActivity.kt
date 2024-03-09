package tool.xfy9326.milink.nfc.activity

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.ui.screen.HomeScreen
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.vm.MainViewModel
import tool.xfy9326.milink.nfc.utils.MIME_ALL
import tool.xfy9326.milink.nfc.utils.showToast
import tool.xfy9326.milink.nfc.utils.startActivity

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private val readNdefBin = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            viewModel.requestNdefBinWriteActivity(it)
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
        bindViewModelData()
    }

    private fun startReaderActivity() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            showToast(R.string.not_support_nfc)
        } else if (!nfcAdapter.isEnabled) {
            showToast(R.string.nfc_disabled)
        } else {
            startActivity<XiaomiNfcReaderActivity>()
        }
    }

    private fun bindViewModelData() {
        lifecycleScope.launch {
            viewModel.instantMsg.collect {
                showToast(it.resId)
            }
        }
        lifecycleScope.launch {
            viewModel.nfcWriteData.collect {
                NdefWriterActivity.openActivity(this@MainActivity, it)
            }
        }
    }
}
