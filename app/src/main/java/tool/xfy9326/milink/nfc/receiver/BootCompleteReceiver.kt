package tool.xfy9326.milink.nfc.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import tool.xfy9326.milink.nfc.service.NfcNotificationListenerService


class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NfcNotificationListenerService.toggleServiceIfStarted(context)
        }
    }
}