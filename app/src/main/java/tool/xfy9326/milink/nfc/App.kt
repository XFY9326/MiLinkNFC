package tool.xfy9326.milink.nfc

import android.app.Application
import android.content.Context
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tool.xfy9326.milink.nfc.db.AppSettings

private var appContext: Context? = null

val AppContext: Context
    get() = appContext ?: error("Application context not initialized")

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        init()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun init() {
        GlobalScope.launch {
            AppSettings.initValues(applicationContext)
        }
    }
}