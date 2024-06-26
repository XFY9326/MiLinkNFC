package tool.xfy9326.milink.nfc.activity

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.core.content.IntentCompat
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NfcActionIntentType
import tool.xfy9326.milink.nfc.datastore.AppDataStore
import tool.xfy9326.milink.nfc.protocol.HuaweiHandoffNfc
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc
import tool.xfy9326.milink.nfc.utils.safeStartActivity
import tool.xfy9326.milink.nfc.utils.showToast

class HuaweiShareNfcActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val bluetoothMac = HuaweiHandoffNfc.parseBluetoothMac(intent)
            if (bluetoothMac == null) {
                applicationContext.showToast(R.string.bluetooth_mac_not_found)
            } else {
                redirectHuaweiNdef(bluetoothMac)
            }
        }
        finish()
    }

    private fun redirectHuaweiNdef(bluetoothMac: String) {
        val nfcTag = IntentCompat.getParcelableExtra(intent, NfcAdapter.EXTRA_TAG, Tag::class.java)
        val nfcId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)

        val huaweiRedirect = runBlocking {
            AppDataStore.getHuaweiRedirect().firstOrNull() ?: AppDataStore.Defaults.huaweiRedirect
        }
        val config = huaweiRedirect.toConfig(bluetoothMac)
        when (huaweiRedirect.actionIntentType) {
            NfcActionIntentType.FAKE_NFC_TAG -> XiaomiNfc.ScreenMirror.newNdefDiscoveredIntent(
                tag = nfcTag,
                id = nfcId,
                config = config
            ).also {
                safeStartActivity(it)
            }

            NfcActionIntentType.MI_CONNECT_SERVICE -> XiaomiNfc.ScreenMirror.sendBroadcast(
                this,
                config
            )
        }
    }
}