package tool.xfy9326.milink.nfc.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.IntentCompat
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.ScreenMirror
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc

class ScreenMirrorActionActivity : Activity() {
    companion object {
        private const val EXTRA_SCREEN_MIRROR = "EXTRA_SCREEN_MIRROR"

        fun newIntent(context: Context, screenMirror: ScreenMirror): Intent =
            Intent(context, ScreenMirrorActionActivity::class.java).apply {
                putExtra(EXTRA_SCREEN_MIRROR, screenMirror)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val screenMirror =
            IntentCompat.getParcelableExtra(intent, EXTRA_SCREEN_MIRROR, ScreenMirror::class.java)
        if (screenMirror != null && screenMirror.bluetoothMac.isNotBlank()) {
            XiaomiNfc.ScreenMirror.sendBroadcast(this, screenMirror.toConfig())
        } else {
            Toast.makeText(applicationContext, R.string.bluetooth_mac_not_set, Toast.LENGTH_SHORT)
                .show()
        }
        finish()
    }
}