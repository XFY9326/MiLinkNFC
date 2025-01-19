package tool.xfy9326.milink.nfc.service

import android.app.PendingIntent
import android.service.quicksettings.TileService
import androidx.core.service.quicksettings.PendingIntentActivityWrapper
import androidx.core.service.quicksettings.TileServiceCompat
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.activity.ScreenMirrorActivity
import tool.xfy9326.milink.nfc.datastore.AppDataStore
import tool.xfy9326.milink.nfc.utils.showToast

class ScreenMirrorTileService : TileService() {
    override fun onClick() {
        val screenMirror = runBlocking {
            AppDataStore.getTilesScreenMirror().firstOrNull()
                ?: AppDataStore.Defaults.tilesScreenMirror
        }
        val pendingIntent = PendingIntentActivityWrapper(
            this,
            1,
            ScreenMirrorActivity.newIntent(this, screenMirror),
            PendingIntent.FLAG_ONE_SHOT,
            false
        )
        runCatching {
            TileServiceCompat.startActivityAndCollapse(this@ScreenMirrorTileService, pendingIntent)
        }.onFailure {
            it.printStackTrace()
            showToast(R.string.activity_start_failed)
        }
    }
}