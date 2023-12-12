package tool.xfy9326.milink.nfc.datastore

import tool.xfy9326.milink.nfc.AppContext
import tool.xfy9326.milink.nfc.data.HuaweiRedirect
import tool.xfy9326.milink.nfc.data.NfcActionIntentType
import tool.xfy9326.milink.nfc.data.ScreenMirror
import tool.xfy9326.milink.nfc.datastore.base.ExtendedDataStore
import tool.xfy9326.milink.nfc.datastore.base.key.booleanDefaultKey
import tool.xfy9326.milink.nfc.datastore.base.key.booleanDefaultLazyKey
import tool.xfy9326.milink.nfc.datastore.base.key.enumDefaultKey
import tool.xfy9326.milink.nfc.datastore.base.key.stringDefaultKey
import tool.xfy9326.milink.nfc.utils.EMPTY
import tool.xfy9326.milink.nfc.utils.MiContinuityUtils

object AppDataStore : ExtendedDataStore("app") {
    val confirmedNotSupportedOSAlert by booleanDefaultKey(defaultValue = false)

    private val tilesScreenMirrorDeviceType by enumDefaultKey(
        defaultValue = ScreenMirror.DeviceType.PC,
        parser = ScreenMirror.DeviceType::valueOf
    )
    private val tilesScreenMirrorIntentType by enumDefaultKey(
        defaultValue = NfcActionIntentType.MI_CONNECT_SERVICE,
        parser = NfcActionIntentType::valueOf
    )
    private val tilesScreenMirrorBluetoothMac by stringDefaultKey(defaultValue = EMPTY)
    private val tilesScreenMirrorEnableLyra by booleanDefaultLazyKey {
        MiContinuityUtils.isLocalDeviceSupportLyra(AppContext)
    }

    private val huaweiRedirectDeviceType by enumDefaultKey(
        defaultValue = ScreenMirror.DeviceType.PC,
        parser = ScreenMirror.DeviceType::valueOf
    )
    private val huaweiRedirectIntentType by enumDefaultKey(
        defaultValue = NfcActionIntentType.FAKE_NFC_TAG,
        parser = NfcActionIntentType::valueOf
    )
    private val huaweiRedirectEnableLyra by booleanDefaultLazyKey {
        MiContinuityUtils.isLocalDeviceSupportLyra(AppContext)
    }

    fun getTilesScreenMirror() =
        readFlow {
            ScreenMirror(
                deviceType = tilesScreenMirrorDeviceType.getEnumValue(it),
                actionIntentType = tilesScreenMirrorIntentType.getEnumValue(it),
                bluetoothMac = tilesScreenMirrorBluetoothMac.getValue(it),
                enableLyra = tilesScreenMirrorEnableLyra.getValue(it),
            )
        }

    suspend fun setTilesScreenMirror(screenMirror: ScreenMirror) {
        edit {
            tilesScreenMirrorDeviceType.setEnumValue(it, screenMirror.deviceType)
            tilesScreenMirrorIntentType.setEnumValue(it, screenMirror.actionIntentType)
            tilesScreenMirrorBluetoothMac.setValue(it, screenMirror.bluetoothMac)
            tilesScreenMirrorEnableLyra.setValue(it, screenMirror.enableLyra)
        }
    }

    fun getHuaweiRedirect() =
        readFlow {
            HuaweiRedirect(
                deviceType = huaweiRedirectDeviceType.getEnumValue(it),
                actionIntentType = huaweiRedirectIntentType.getEnumValue(it),
                enableLyra = huaweiRedirectEnableLyra.getValue(it),
            )
        }

    suspend fun setHuaweiRedirect(huaweiRedirect: HuaweiRedirect) {
        edit {
            huaweiRedirectDeviceType.setEnumValue(it, huaweiRedirect.deviceType)
            huaweiRedirectIntentType.setEnumValue(it, huaweiRedirect.actionIntentType)
            huaweiRedirectEnableLyra.setValue(it, huaweiRedirect.enableLyra)
        }
    }

    object Defaults {
        val tilesScreenMirror = ScreenMirror(
            deviceType = tilesScreenMirrorDeviceType.defaultEnumValue(),
            actionIntentType = tilesScreenMirrorIntentType.defaultEnumValue(),
            bluetoothMac = tilesScreenMirrorBluetoothMac.defaultValue(),
            enableLyra = true
        )
        val huaweiRedirect = HuaweiRedirect(
            deviceType = huaweiRedirectDeviceType.defaultEnumValue(),
            actionIntentType = huaweiRedirectIntentType.defaultEnumValue(),
            enableLyra = true
        )
    }
}