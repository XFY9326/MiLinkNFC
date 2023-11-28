package tool.xfy9326.milink.nfc.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import tool.xfy9326.milink.nfc.service.NfcNotificationListenerService
import tool.xfy9326.milink.nfc.utils.isUsingNotificationService


class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
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
}