package tool.xfy9326.milink.nfc.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


val Context.packageUri: Uri
    get() = "package:${packageName}".toUri()

inline fun <reified A : Activity> Context.startActivity(intentBlock: Intent.() -> Unit = {}) {
    startActivity(Intent(this, A::class.java).apply(intentBlock))
}

fun Context.showToast(@StringRes resId: Int, vararg formatArgs: Any, longDuration: Boolean = false): Unit =
    showToast(getString(resId, *formatArgs), longDuration)

fun Context.showToast(text: String, longDuration: Boolean = false): Unit =
    Toast.makeText(this, text, if (longDuration) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()

suspend fun Context.showToastInMain(@StringRes resId: Int, vararg formatArgs: Any, longDuration: Boolean = false): Unit =
    showToastInMain(getString(resId, *formatArgs), longDuration)

suspend fun Context.showToastInMain(text: String, longDuration: Boolean = false): Unit =
    withContext(Dispatchers.Main.immediate) { showToast(text, longDuration) }

fun Context.isUsingNotificationService(): Boolean {
    return NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
}

fun Context.openAppSettings() {
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri))
}

fun Context.openNotificationServiceSettings(componentName: String) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
            putExtra(
                Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                ComponentName(packageName, componentName).flattenToString()
            )
        }
    } else {
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            val value = "$packageName/$componentName"
            val key = ":settings:fragment_args_key"
            putExtra(key, value)
            putExtra(":settings:show_fragment_args", Bundle().also { it.putString(key, value) })
        }
    }
    startActivity(intent)
}