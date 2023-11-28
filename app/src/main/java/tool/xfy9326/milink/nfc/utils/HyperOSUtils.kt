package tool.xfy9326.milink.nfc.utils

import android.annotation.SuppressLint


private const val KEY_MI_OS_VERSION_CODE = "ro.mi.os.version.code"
private const val KEY_MI_OS_VERSION_NAME = "ro.mi.os.version.name"
private const val KEY_MI_OS_VERSION_INCREMENTAL = "ro.mi.os.version.incremental"

@SuppressLint("PrivateApi")
fun isXiaomiHyperOS(): Boolean = runCatching {
    val cls = Class.forName("android.os.SystemProperties")
    val get = cls.getMethod("get", String::class.java)
    get(null, KEY_MI_OS_VERSION_CODE) != null ||
            get(null, KEY_MI_OS_VERSION_NAME) != null ||
            get(null, KEY_MI_OS_VERSION_INCREMENTAL) != null
}.getOrDefault(false)
