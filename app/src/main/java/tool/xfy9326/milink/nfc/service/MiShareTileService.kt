package tool.xfy9326.milink.nfc.service

import android.app.PendingIntent
import android.service.quicksettings.TileService
import androidx.core.service.quicksettings.PendingIntentActivityWrapper
import androidx.core.service.quicksettings.TileServiceCompat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import tool.xfy9326.milink.nfc.activity.MiConnectServiceActivity
import tool.xfy9326.milink.nfc.data.XiaomiMirrorType
import tool.xfy9326.milink.nfc.data.toXiaomiDeviceType
import tool.xfy9326.milink.nfc.data.toXiaomiMirrorType
import tool.xfy9326.milink.nfc.db.AppSettings
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc

class MiShareTileService : TileService() {
    companion object {
        private const val TILE_ACTIVITY_REQUEST_CODE = 1
    }

    override fun onClick() {
        val globalSettings = runBlocking { AppSettings.global.data.first() }
        val btMac = globalSettings.tilesNfcBtMac.takeIf { it.isNotBlank() }
        if (btMac == null) {
            MiConnectServiceActivity.newIntent(this@MiShareTileService, null, null)
        } else {
            val deviceType = globalSettings.tilesNfcDevice.toXiaomiDeviceType(AppSettings.GlobalDefaults.tilesNfcDevice)
            val enableLyra = if (globalSettings.hasTilesEnableLyra()) {
                globalSettings.tilesEnableLyra.value
            } else {
                AppSettings.GlobalDefaults.tilesEnableLyra
            }
            when (globalSettings.tilesMirrorIntent.toXiaomiMirrorType(AppSettings.GlobalDefaults.tilesMirrorIntent)) {
                XiaomiMirrorType.MI_CONNECT_SERVICE ->
                    MiConnectServiceActivity.newIntent(this@MiShareTileService, deviceType, btMac, enableLyra)

                XiaomiMirrorType.FAKE_NFC_TAG ->
                    XiaomiNfc.newNdefActivityIntent(null, null, deviceType.nfcType, btMac, enableLyra)
            }
        }.let {
            PendingIntentActivityWrapper(this@MiShareTileService, TILE_ACTIVITY_REQUEST_CODE, it, PendingIntent.FLAG_ONE_SHOT, false)
        }.let {
            TileServiceCompat.startActivityAndCollapse(this@MiShareTileService, it)
        }
    }
}