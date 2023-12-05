package tool.xfy9326.milink.nfc.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.XiaomiDeviceType
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc

class MiConnectServiceActivity : Activity() {
    companion object {
        private const val EXTRA_BT_MAC = "EXTRA_BT_MAC"
        private const val EXTRA_DEVICE_TYPE = "EXTRA_DEVICE_TYPE"
        private const val EXTRA_ENABLE_LYRA = "EXTRA_ENABLE_LYRA"

        val DEFAULT_NFC_DEVICE = XiaomiDeviceType.PC

        fun newIntent(context: Context, xiaomiDeviceType: XiaomiDeviceType?, btMac: String?, enableLyra: Boolean = false): Intent =
            Intent(context, MiConnectServiceActivity::class.java).apply {
                putExtra(EXTRA_BT_MAC, btMac)
                putExtra(EXTRA_DEVICE_TYPE, xiaomiDeviceType?.name)
                putExtra(EXTRA_ENABLE_LYRA, enableLyra)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val btMac = intent.getStringExtra(EXTRA_BT_MAC)
        val xiaomiDeviceType = intent.getStringExtra(EXTRA_DEVICE_TYPE)?.let { XiaomiDeviceType.valueOf(it) } ?: DEFAULT_NFC_DEVICE
        val enableLyra = intent.getBooleanExtra(EXTRA_ENABLE_LYRA, false)
        if (btMac != null) {
            XiaomiNfc.sendConnectServiceBroadcast(this, xiaomiDeviceType.nfcType, btMac, enableLyra)
        } else {
            Toast.makeText(applicationContext, R.string.bt_mac_not_set, Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}