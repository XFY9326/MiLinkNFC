package tool.xfy9326.milink.nfc.activity

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.IntentCompat
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NfcActionIntentType
import tool.xfy9326.milink.nfc.data.ScreenMirror
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc
import tool.xfy9326.milink.nfc.utils.EMPTY
import tool.xfy9326.milink.nfc.utils.safeStartActivity
import tool.xfy9326.milink.nfc.utils.showToast

class ScreenMirrorActivity : Activity() {
    companion object {
        private const val EXTRA_SCREEN_MIRROR = "EXTRA_SCREEN_MIRROR"

        private const val ACTION_POSTFIX = ".action.screen_mirror"

        private const val EXTRA_DEVICE_TYPE = "device"
        private const val EXTRA_ACTION_INTENT_TYPE = "action"
        private const val EXTRA_BLUETOOTH_MAC = "btMac"
        private const val EXTRA_ENABLE_LYRA = "lyra"

        private val DEFAULT_DEVICE_TYPE = ScreenMirror.DeviceType.PC
        private val DEFAULT_ACTION_INTENT_TYPE = NfcActionIntentType.MI_CONNECT_SERVICE
        private const val DEFAULT_BLUETOOTH_MAC = EMPTY
        private const val DEFAULT_ENABLE_LYRA = true

        private fun Intent.readDeviceType() =
            when (getIntExtra(EXTRA_DEVICE_TYPE, -1)) {
                0 -> ScreenMirror.DeviceType.TV
                1 -> ScreenMirror.DeviceType.PC
                2 -> ScreenMirror.DeviceType.CAR
                3 -> ScreenMirror.DeviceType.PAD
                else -> DEFAULT_DEVICE_TYPE
            }

        private fun Intent.readActionIntentType() =
            when (getIntExtra(EXTRA_ACTION_INTENT_TYPE, -1)) {
                0 -> NfcActionIntentType.FAKE_NFC_TAG
                1 -> NfcActionIntentType.MI_CONNECT_SERVICE
                else -> DEFAULT_ACTION_INTENT_TYPE
            }

        private fun Intent.readExternalScreenMirror() =
            ScreenMirror(
                deviceType = readDeviceType(),
                actionIntentType = readActionIntentType(),
                bluetoothMac = getStringExtra(EXTRA_BLUETOOTH_MAC)?.trim() ?: DEFAULT_BLUETOOTH_MAC,
                enableLyra = getBooleanExtra(EXTRA_ENABLE_LYRA, DEFAULT_ENABLE_LYRA)
            )

        private fun Intent.readIntentScreenMirror(context: Context) =
            if (action == context.packageName + ACTION_POSTFIX) {
                readExternalScreenMirror()
            } else if (hasExtra(EXTRA_SCREEN_MIRROR)) {
                IntentCompat.getParcelableExtra(
                    this,
                    EXTRA_SCREEN_MIRROR,
                    ScreenMirror::class.java
                )
            } else {
                null
            }

        fun newIntent(context: Context, screenMirror: ScreenMirror): Intent =
            Intent(context, ScreenMirrorActivity::class.java).apply {
                putExtra(EXTRA_SCREEN_MIRROR, screenMirror)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.readIntentScreenMirror(this)?.let {
            launchScreenMirror(it)
        }
        finish()
    }

    private fun launchScreenMirror(screenMirror: ScreenMirror) {
        if (screenMirror.bluetoothMac.isBlank()) {
            applicationContext.showToast(R.string.bluetooth_mac_not_set)
            return
        }
        if (!BluetoothAdapter.checkBluetoothAddress(screenMirror.bluetoothMac)) {
            applicationContext.showToast(R.string.bluetooth_invalid_mac_address)
            return
        }
        when (screenMirror.actionIntentType) {
            NfcActionIntentType.FAKE_NFC_TAG -> safeStartActivity(
                XiaomiNfc.ScreenMirror.newNdefDiscoveredIntent(null, null, screenMirror.toConfig())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )

            NfcActionIntentType.MI_CONNECT_SERVICE ->
                XiaomiNfc.ScreenMirror.sendBroadcast(this, screenMirror.toConfig())
        }
    }
}