package tool.xfy9326.milink.nfc.utils

import android.net.Uri
import tool.xfy9326.milink.nfc.AppContext

fun Uri.writeBinary(bytes: ByteArray): Boolean = runCatching {
    AppContext.contentResolver.openOutputStream(this)!!.use {
        it.write(bytes)
    }
}.isSuccess