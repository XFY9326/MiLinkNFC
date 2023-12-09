package tool.xfy9326.milink.nfc.protocol

import android.content.Context
import android.nfc.NdefMessage
import tool.xfy9326.milink.nfc.data.PackageData
import tool.xfy9326.milink.nfc.utils.getPackageData
import tool.xfy9326.milink.nfc.utils.getPackageMetaData

object XiaomiMirrorNfc {
    private const val ACTION_TAG_DISCOVERED = "TAG_DISCOVERED"
    private const val ACTION_SUFFIX_MIRROR = "MIRROR"

    private const val FLAG_ABILITY_LYRA = 0x00000001.toByte()

    private const val PKG_MI_LINK_SERVICE = "com.milink.service"
    private const val PKG_MI_CONNECT_SERVICE = "com.xiaomi.mi_connect_service"

    private const val PKG_META_DATA_XIAOMI_CONTINUITY_VERSION_NAME = "com.xiaomi.continuity.VERSION_NAME"

    private val ONE_HOP_RELATED_PACKAGE_NAMES = arrayOf(
        PKG_MI_LINK_SERVICE,
        PKG_MI_CONNECT_SERVICE,
    )

    fun createNdefMessage(deviceType: XiaomiNfc.NewProtocol.DeviceType, btMac: String, enableLyra: Boolean): NdefMessage {
        val payload = XiaomiNfc.NewProtocol.buildNdefRecordPayload(buildAppData(deviceType, btMac, enableLyra))
        return XiaomiNfc.createNdefMessage(payload)
    }

    fun sendConnectServiceBroadcast(context: Context, deviceType: XiaomiNfc.NewProtocol.DeviceType, btMac: String, enableLyra: Boolean) {
        XiaomiNfc.NewProtocol.sendBroadcast(context, buildAppData(deviceType, btMac, enableLyra))
    }

    fun getRelatedPackageDataMap(context: Context): Map<String, PackageData?> =
        ONE_HOP_RELATED_PACKAGE_NAMES.associateWith { context.getPackageData(it) }

    fun isLocalDeviceSupportLyra(context: Context): Boolean {
        return context.getPackageMetaData(PKG_MI_CONNECT_SERVICE)?.getString(PKG_META_DATA_XIAOMI_CONTINUITY_VERSION_NAME) != null
    }

    private fun buildAppData(deviceType: XiaomiNfc.NewProtocol.DeviceType, btMac: String, enableLyra: Boolean) =
        XiaomiNfc.NewProtocol.AppData(
            deviceType = deviceType,
            action = ACTION_TAG_DISCOVERED,
            payloadMap = buildPayloadMap(btMac, enableLyra)
        )

    private fun buildPayloadMap(btMac: String, enableLyra: Boolean) =
        mutableMapOf(
            XiaomiNfc.NewProtocol.AttributeType.ACTION_SUFFIX to ACTION_SUFFIX_MIRROR.toByteArray(Charsets.UTF_8),
            XiaomiNfc.NewProtocol.AttributeType.BT_MAC to btMac.toByteArray(Charsets.UTF_8)
        ).also {
            if (enableLyra) {
                it[XiaomiNfc.NewProtocol.AttributeType.EXT_ABILITY] = byteArrayOf(FLAG_ABILITY_LYRA)
            }
        }
}