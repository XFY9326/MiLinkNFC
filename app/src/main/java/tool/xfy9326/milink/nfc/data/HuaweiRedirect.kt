package tool.xfy9326.milink.nfc.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tool.xfy9326.milink.nfc.db.AppSettings
import tool.xfy9326.milink.nfc.proto.AppSettingsProto

data class HuaweiRedirectData(
    val deviceType: XiaomiDeviceType,
    val mirrorType: XiaomiMirrorType,
    val enableLyra: Boolean
)

fun Flow<AppSettingsProto.GlobalSettings>.getHuaweiRedirectData(): Flow<HuaweiRedirectData> = map {
    val deviceType = it.huaweiRedirectNfcDevice.toXiaomiDeviceType(AppSettings.GlobalDefaults.huaweiRedirectNfcDevice)
    val mirrorIntent = it.huaweiRedirectMirrorIntent.toXiaomiMirrorType(AppSettings.GlobalDefaults.huaweiRedirectMirrorIntent)
    val enableLyra = if (it.hasHuaweiRedirectEnableLyra()) it.huaweiRedirectEnableLyra.value else AppSettings.GlobalDefaults.huaweiRedirectEnableLyra
    HuaweiRedirectData(deviceType, mirrorIntent, enableLyra)
}
