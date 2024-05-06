package tool.xfy9326.milink.nfc.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.getSystemService
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.ui.screen.HomeScreen
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.vm.MainViewModel
import tool.xfy9326.milink.nfc.utils.BluetoothMacAddressScanner
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
    private val bluetoothScanner = BluetoothMacAddressScanner(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                HomeScreen(
                    supportScanBluetoothMac = bluetoothScanner.isSupported,
                    onNavToXiaomiNfcReader = ::startReaderActivity,
                    onRequestWriteNdefBin = { readNdefBin.launch(MIME_ALL) },
                    onRequestScanBluetoothMac = ::scanBluetoothMac,
                    onExit = ::finishAndRemoveTask
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
            startActivity<NdefReaderActivity>()
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

    private fun scanBluetoothMac() {
        if (bluetoothScanner.isEnabled) {
            bluetoothScanner.requestDevice { address ->
                if (address == null) {
                    showToast(R.string.bluetooth_mac_scan_failure)
                } else {
                    getSystemService<ClipboardManager>()?.let {
                        it.setPrimaryClip(ClipData.newPlainText(address, address))
                        showToast(R.string.bluetooth_mac_scan_success, address)
                    }
                }
            }
        } else {
            showToast(R.string.bluetooth_disabled)
        }
    }
}
