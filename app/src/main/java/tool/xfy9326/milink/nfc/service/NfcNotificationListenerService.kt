package tool.xfy9326.milink.nfc.service

import android.app.Notification
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import tool.xfy9326.milink.nfc.protocol.HuaweiNfc
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc

class NfcNotificationListenerService : NotificationListenerService() {
    companion object {
        private const val PACKAGE_NAME_NFC = "com.android.nfc"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        requestRebind(ComponentName(applicationContext, NfcNotificationListenerService::class.java))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == PACKAGE_NAME_NFC && sbn.notification.channelId == XiaomiNfc.NFC_NOTIFICATION_CHANNEL_ID) {
            val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT) ?: return
            if (HuaweiNfc.NFC_URI_CONTENT in text && sbn.notification.actions.isNotEmpty()) {
                val cancelStr = getString(android.R.string.cancel)
                sbn.notification.actions.first { it.title != cancelStr }?.actionIntent?.send()
            }
        }
    }
}