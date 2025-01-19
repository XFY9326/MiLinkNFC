package tool.xfy9326.milink.nfc.data.nfc

import android.content.Intent
import android.net.Uri
import android.nfc.NdefRecord
import tool.xfy9326.milink.nfc.utils.toHexText

data class RawNdefData(
    val id: String?,
    val tnf: NdefData.TNF,
    val rtd: NdefData.RTD?,
    val typeText: String?,
    val typeHex: String?,
    val payloadLanguage: String?,
    private val payloadText: String?,
    private val payloadHex: String?
) : NdefData {
    companion object {
        private fun NdefRecord.getRTDText() =
            if (
                tnf == NdefRecord.TNF_WELL_KNOWN &&
                type.contentEquals(NdefRecord.RTD_TEXT) &&
                payload.size > 2
            ) NdefData.parseRTDText(payload) else null

        fun parse(record: NdefRecord): RawNdefData {
            val payloadRTDText = record.getRTDText()
            return RawNdefData(
                id = record.id?.takeIf { it.isNotEmpty() }?.toHexText(),
                tnf = NdefData.TNF.getByValue(record.tnf.toByte()),
                rtd = NdefData.RTD.getByValue(record.type),
                typeText = record.type.takeIf { it.isNotEmpty() }?.runCatching {
                    when (record.tnf) {
                        NdefRecord.TNF_ABSOLUTE_URI ->
                            Uri.parse(toString(Charsets.UTF_8)).normalizeScheme().toString()

                        NdefRecord.TNF_MIME_MEDIA ->
                            Intent.normalizeMimeType(toString(Charsets.US_ASCII))

                        else -> null
                    }
                }?.onFailure {
                    it.printStackTrace()
                }?.getOrNull(),
                typeHex = record.type?.takeIf { it.isNotEmpty() }?.toHexText(),
                payloadLanguage = payloadRTDText?.first,
                payloadText = record.payload?.takeIf { it.isNotEmpty() }?.runCatching {
                    if (
                        record.tnf == NdefRecord.TNF_EXTERNAL_TYPE &&
                        record.type.contentEquals(NdefData.RTD.ANDROID_APP.value)
                    ) {
                        record.payload.toString(Charsets.UTF_8)
                    } else if (
                        record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                        record.type.contentEquals(NdefRecord.RTD_TEXT)
                    ) {
                        payloadRTDText?.second
                    } else if (
                        record.tnf == NdefRecord.TNF_MIME_MEDIA &&
                        record.toMimeType() == "text/plain"
                    ) {
                        record.payload.toString(Charsets.UTF_8)
                    } else if (
                        record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                        record.type.contentEquals(NdefRecord.RTD_URI)
                    ) {
                        record.toUri()?.toString()
                    } else null
                }?.onFailure {
                    it.printStackTrace()
                }?.getOrNull(),
                payloadHex = record.payload?.takeIf { it.isNotEmpty() }?.toHexText(),
            )
        }
    }

    val type: String?
        get() = if (rtd == null) {
            typeText ?: typeHex
        } else if (typeText != null) {
            rtd.name + "\n" + typeText
        } else {
            rtd.name
        }

    val payload: String?
        get() = payloadText ?: payloadHex
}