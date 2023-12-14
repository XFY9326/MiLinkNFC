package tool.xfy9326.milink.nfc.utils

import android.nfc.NdefMessage

object NdefIO {
    private const val NDEF_PREFIX = "<NdefMessage>"
    private const val NDEF_SUFFIX = "</NdefMessage>"

    fun readNdefMessage(bytes: ByteArray): NdefMessage? =
        runCatching {
            NdefMessage(bytes)
        }.getOrElse {
            readNxpNdefMessage(bytes)
        }

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
