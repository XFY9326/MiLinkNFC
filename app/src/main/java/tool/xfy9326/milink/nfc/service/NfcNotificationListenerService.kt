package tool.xfy9326.milink.nfc.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.content.getSystemService
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.protocol.HuaweiHandoffNfc
import tool.xfy9326.milink.nfc.utils.isUsingNotificationService

class NfcNotificationListenerService : NotificationListenerService() {
    companion object {
        private const val PACKAGE_NAME_NFC = "com.android.nfc"
        private const val FOREGROUND_ID = 1024
        private const val FOREGROUND_CHANNEL_ID = "foreground"
        private const val NFC_NOTIFICATION_CHANNEL_ID = "tag_dispatch"

        fun toggleServiceIfStarted(context: Context) {
            if (context.isUsingNotificationService()) {
                val packageManager = context.packageManager
                val componentName = ComponentName(context, NfcNotificationListenerService::class.java)
                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        getSystemService<NotificationManager>()?.let {
            val channel = it.getNotificationChannel(FOREGROUND_CHANNEL_ID) ?: NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                getString(R.string.notification_channel_foreground_service),
                NotificationManager.IMPORTANCE_MIN
            ).also { channel ->
                it.createNotificationChannel(channel)
            }
            Notification.Builder(this, channel.id).build()
        }?.let {
            startForeground(FOREGROUND_ID, it)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == PACKAGE_NAME_NFC && sbn.notification.channelId == NFC_NOTIFICATION_CHANNEL_ID) {
            val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT) ?: return
            if (HuaweiHandoffNfc.NFC_URI_CONTENT in text && sbn.notification.actions.isNotEmpty()) {
                val cancelStr = getString(android.R.string.cancel)
                sbn.notification.actions.first { it.title != cancelStr }?.actionIntent?.send()
            }
        }
    }
}