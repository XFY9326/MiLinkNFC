package tool.xfy9326.milink.nfc.utils

import android.net.Uri
import android.nfc.NdefMessage

object NdefIO {
    private const val FILE_EXT_BIN = "bin"
    private const val FILE_EXT_NXP_NDEF = "ndef"

    private const val NDEF_PREFIX = "<NdefMessage>"
    private const val NDEF_SUFFIX = "</NdefMessage>"

    fun readNdefMessage(bytes: ByteArray): NdefMessage? =
        runCatching {
            NdefMessage(bytes)
        }.getOrElse {
            readNxpNdefMessage(bytes)
        }

    fun writeNdefMessage(uri: Uri, bytes: ByteArray, isNxpNdefFormat: Boolean): Boolean =
        if (isNxpNdefFormat) {
            uri.writeText(writeNxpNdefMessage(bytes))
        } else {
            uri.writeBinary(bytes)
        }

    fun getExportFileName(scanTime: Long, isNxpNdefFormat: Boolean): String {
        return "NDEF_${scanTime}." + if (isNxpNdefFormat) {
            FILE_EXT_NXP_NDEF
        } else {
            FILE_EXT_BIN
        }
    }

    private fun writeNxpNdefMessage(bytes: ByteArray): String =
        NDEF_PREFIX + bytes.toHexString() + NDEF_SUFFIX

    private fun readNxpNdefMessage(bytes: ByteArray): NdefMessage? = runCatching {
        val content = bytes.toString(Charsets.UTF_8).trim()
        if (content.startsWith(NDEF_PREFIX) && content.endsWith(NDEF_SUFFIX)) {
            val hex = content.substringAfter(NDEF_PREFIX).substringBeforeLast(NDEF_SUFFIX).trim()
            NdefMessage(hex.hexToByteArray())
        } else {
            null
        }
    }.getOrNull()
}
