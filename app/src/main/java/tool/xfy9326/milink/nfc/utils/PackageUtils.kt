package tool.xfy9326.milink.nfc.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import tool.xfy9326.milink.nfc.data.PackageData

@SuppressLint("QueryPermissionsNeeded")
fun Context.getPackageData(packageName: String): PackageData? =
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
    }.getOrNull()?.runCatching {
        PackageData(
            applicationName = applicationInfo.loadLabel(packageManager).toString(),
            packageName = packageName,
            versionCode = longVersionCode,
            versionName = versionName,
            icon = applicationInfo.loadUnbadgedIcon(packageManager)
        )
    }?.onFailure {
        it.printStackTrace()
    }?.getOrNull()
