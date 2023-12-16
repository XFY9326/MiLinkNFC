package tool.xfy9326.milink.nfc.utils

import android.content.Context
import tool.xfy9326.milink.nfc.data.PackageData

object MiContinuityUtils {
    private const val PKG_MI_LINK_SERVICE = "com.milink.service"
    private const val PKG_MI_CONNECT_SERVICE = "com.xiaomi.mi_connect_service"
    private const val PKG_MIRROR = "com.xiaomi.mirror"
    private const val PKG_FILE_EXPLORER = "com.android.fileexplorer"
    private const val PKG_GALLERY = "com.miui.gallery"

    private const val PKG_META_DATA_XIAOMI_CONTINUITY_VERSION_NAME = "com.xiaomi.continuity.VERSION_NAME"

    private val ONE_TAP_RELATED_PACKAGE_NAMES = arrayOf(
        PKG_MI_CONNECT_SERVICE,
        PKG_MI_LINK_SERVICE,
        PKG_MIRROR,
        PKG_FILE_EXPLORER,
        PKG_GALLERY
    )

    fun getNfcRelatedPackageDataMap(context: Context): Map<String, PackageData?> =
        ONE_TAP_RELATED_PACKAGE_NAMES.associateWith { context.getPackageData(it) }

    fun isLocalDeviceSupportLyra(context: Context): Boolean {
        return context.getPackageMetaData(PKG_MI_CONNECT_SERVICE)?.getString(PKG_META_DATA_XIAOMI_CONTINUITY_VERSION_NAME) != null
    }
}