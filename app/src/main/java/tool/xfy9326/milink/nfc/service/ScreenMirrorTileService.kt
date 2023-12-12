package tool.xfy9326.milink.nfc.service

import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.TileService
import androidx.core.service.quicksettings.PendingIntentActivityWrapper
import androidx.core.service.quicksettings.TileServiceCompat
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import tool.xfy9326.milink.nfc.activity.ScreenMirrorActionActivity
import tool.xfy9326.milink.nfc.data.NfcActionIntentType
import tool.xfy9326.milink.nfc.datastore.AppDataStore
import tool.xfy9326.milink.nfc.protocol.XiaomiNfc

class ScreenMirrorTileService : TileService() {
    override fun onClick() {
        val screenMirror = runBlocking { AppDataStore.getTilesScreenMirror().firstOrNull() ?: AppDataStore.Defaults.tilesScreenMirror }
        when (screenMirror.actionIntentType) {
            NfcActionIntentType.FAKE_NFC_TAG ->
                XiaomiNfc.ScreenMirror.newNdefDiscoveredIntent(null, null, screenMirror.toConfig())

            NfcActionIntentType.MI_CONNECT_SERVICE ->
                ScreenMirrorActionActivity.newIntent(this@ScreenMirrorTileService, screenMirror)
        }.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            PendingIntentActivityWrapper(this@ScreenMirrorTileService, 1, it, PendingIntent.FLAG_ONE_SHOT, false)
        }.let {
            TileServiceCompat.startActivityAndCollapse(this@ScreenMirrorTileService, it)
        }
    }
}