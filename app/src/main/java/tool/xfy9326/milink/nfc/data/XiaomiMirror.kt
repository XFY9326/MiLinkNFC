package tool.xfy9326.milink.nfc.data

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.parcelize.Parcelize
import lib.xfy9326.xiaomi.nfc.HandoffAppData
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.db.AppSettings
import tool.xfy9326.milink.nfc.proto.AppSettingsProto

@Parcelize
data class XiaomiMirrorData(
    val deviceType: XiaomiDeviceType,
    val mirrorType: XiaomiMirrorType,
    val btMac: String,
    val enableLyra: Boolean
) : Parcelable

enum class XiaomiDeviceType(
    val nfcType: HandoffAppData.DeviceType,
    val protoType: AppSettingsProto.NfcDevice,
    @StringRes val resId: Int
) {
    TV(HandoffAppData.DeviceType.TV, AppSettingsProto.NfcDevice.TV, R.string.device_type_tv),
    PC(HandoffAppData.DeviceType.PC, AppSettingsProto.NfcDevice.PC, R.string.device_type_pc),
    CAR(HandoffAppData.DeviceType.CAR, AppSettingsProto.NfcDevice.CAR, R.string.device_type_car),
    PAD(HandoffAppData.DeviceType.PAD, AppSettingsProto.NfcDevice.PAD, R.string.device_type_pad);
}

enum class XiaomiMirrorType(
    val protoType: AppSettingsProto.MirrorIntent,
    @StringRes val resId: Int
) {
    FAKE_NFC_TAG(AppSettingsProto.MirrorIntent.FAKE_NFC_TAG, R.string.mirror_intent_fake_nfc_tag),
    MI_CONNECT_SERVICE(AppSettingsProto.MirrorIntent.MI_CONNECT_SERVICE, R.string.mirror_intent_mi_connect_service);
}

fun Flow<AppSettingsProto.GlobalSettings>.getTilesXiaomiMirrorData(): Flow<XiaomiMirrorData> = map {
    val deviceType = it.tilesNfcDevice.toXiaomiDeviceType(AppSettings.GlobalDefaults.tilesNfcDevice)
    val mirrorIntent = it.tilesMirrorIntent.toXiaomiMirrorType(AppSettings.GlobalDefaults.tilesMirrorIntent)
    val btMac = it.tilesNfcBtMac
    val enableLyra = if (it.hasTilesEnableLyra()) it.tilesEnableLyra.value else AppSettings.GlobalDefaults.tilesEnableLyra
    XiaomiMirrorData(deviceType, mirrorIntent, btMac, enableLyra)
}

fun AppSettingsProto.MirrorIntent.toXiaomiMirrorType(default: AppSettingsProto.MirrorIntent? = null): XiaomiMirrorType {
    require(default != AppSettingsProto.MirrorIntent.UNKNOWN_MIRROR_INTENT) { "Default mirror type can't be unknown" }
    return when (this) {
        AppSettingsProto.MirrorIntent.FAKE_NFC_TAG -> XiaomiMirrorType.FAKE_NFC_TAG
        AppSettingsProto.MirrorIntent.MI_CONNECT_SERVICE -> XiaomiMirrorType.MI_CONNECT_SERVICE
        AppSettingsProto.MirrorIntent.UNRECOGNIZED, AppSettingsProto.MirrorIntent.UNKNOWN_MIRROR_INTENT ->
            default?.toXiaomiMirrorType() ?: error("No default value")
    }
}

fun AppSettingsProto.NfcDevice.toXiaomiDeviceType(default: AppSettingsProto.NfcDevice? = null): XiaomiDeviceType {
    require(default != AppSettingsProto.NfcDevice.UNRECOGNIZED) { "Default device type can't be unknown" }
    return when (this) {
        AppSettingsProto.NfcDevice.TV -> XiaomiDeviceType.TV
        AppSettingsProto.NfcDevice.PC -> XiaomiDeviceType.PC
        AppSettingsProto.NfcDevice.CAR -> XiaomiDeviceType.CAR
        AppSettingsProto.NfcDevice.PAD -> XiaomiDeviceType.PAD
        AppSettingsProto.NfcDevice.UNRECOGNIZED, AppSettingsProto.NfcDevice.UNKNOWN_NFC_DEVICE ->
            default?.toXiaomiDeviceType() ?: error("No default value")
    }
}
