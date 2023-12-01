package tool.xfy9326.milink.nfc.data

import android.graphics.drawable.Drawable

data class PackageData(
    val applicationName: String,
    val packageName: String,
    val versionCode: Long,
    val versionName: String?,
    val icon: Drawable
)
