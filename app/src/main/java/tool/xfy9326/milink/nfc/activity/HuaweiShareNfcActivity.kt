package tool.xfy9326.milink.nfc.activity

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.XiaomiMirrorType
import tool.xfy9326.milink.nfc.data.toXiaomiDeviceType
import tool.xfy9326.milink.nfc.data.toXiaomiMirrorType
import tool.xfy9326.milink.nfc.db.AppSettings
import tool.xfy9326.milink.nfc.protocol.HuaweiNfc
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc

class HuaweiShareNfcActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val btMac = HuaweiNfc.parseBtMac(intent)
            if (btMac == null) {
                Toast.makeText(applicationContext, R.string.bt_mac_not_found, Toast.LENGTH_SHORT).show()
            } else {
                val tag = IntentCompat.getParcelableExtra(intent, NfcAdapter.EXTRA_TAG, Tag::class.java)
                val id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)

                val globalSettings = runBlocking { AppSettings.global.data.first() }
                val deviceType = globalSettings.huaweiRedirectNfcDevice.toXiaomiDeviceType(AppSettings.GlobalDefaults.huaweiRedirectNfcDevice)
                when (globalSettings.huaweiRedirectMirrorIntent.toXiaomiMirrorType(AppSettings.GlobalDefaults.huaweiRedirectMirrorIntent)) {
                    XiaomiMirrorType.FAKE_NFC_TAG -> {
                        XiaomiNfc.newNdefActivityIntent(tag, id, deviceType.nfcType, btMac).also {
                            ContextCompat.startActivity(this, it, null)
                        }
                    }

                    XiaomiMirrorType.MI_CONNECT_SERVICE -> {
                        XiaomiNfc.sendConnectServiceBroadcast(this, deviceType.nfcType, btMac)
                    }
                }
            }
        }
        finish()
    }
}