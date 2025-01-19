package tool.xfy9326.milink.nfc.utils

import android.net.Uri
import tool.xfy9326.milink.nfc.AppContext

fun Uri.writeText(text: String): Boolean = writeBinary(text.toByteArray(Charsets.UTF_8))

fun Uri.writeBinary(bytes: ByteArray): Boolean = runCatching {
    AppContext.contentResolver.openOutputStream(this)!!.use {
        it.write(bytes)
    }
}.onFailure {
    it.printStackTrace()
}.isSuccess

fun Uri.readBinary(): ByteArray? = runCatching {
    AppContext.contentResolver.openInputStream(this)!!.use {
        it.readBytes()
    }
}.onFailure {
    it.printStackTrace()
}.getOrNull()
