package tool.xfy9326.milink.nfc

import android.app.Application
import android.content.Context

private var appContext: Context? = null

val AppContext: Context
    get() = appContext ?: error("Application context not initialized")

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}