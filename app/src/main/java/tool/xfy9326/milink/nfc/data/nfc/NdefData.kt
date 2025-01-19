package tool.xfy9326.milink.nfc.data.nfc

import android.nfc.NdefRecord

sealed interface NdefData {
    companion object {
        fun parseRTDText(bytes: ByteArray): Pair<String, String>? = runCatching {
            val status = bytes[0].toInt()
            if (bytes.size > 1 + status) {
                val code = bytes.copyOfRange(1, 1 + status).toString(Charsets.US_ASCII)
                val text = bytes.copyOfRange(1 + status, bytes.size)
                    .toString(Charsets.UTF_8)
                code to text
            } else null
        }.getOrNull()

        fun parseWellKnown(record: NdefRecord, inSmartPoster: Boolean = false): NdefData {
            if (!inSmartPoster && SmartPosterNdefData.checkType(record)) {
                runCatching {
                    SmartPosterNdefData.parse(record)
                }.onFailure {
                    it.printStackTrace()
                }.getOrNull()?.let { return it }
            }
            return RawNdefData.parse(record)
        }
    }

    enum class TNF(val value: Byte) {
        EMPTY(0x00),
        WELL_KNOWN(0x01),
        MIME_MEDIA(0x02),
        ABSOLUTE_URI(0x03),
        EXTERNAL_TYPE(0x04),
        UNKNOWN(0x05),
        UNCHANGED(0x06),
        RESERVED(0x07);

        companion object {
            fun getByValue(value: Byte): TNF =
                entries.firstOrNull { it.value == value } ?: UNKNOWN
        }
    }

    enum class RTD(val value: ByteArray) {
        TEXT(byteArrayOf(0x54)),
        URI(byteArrayOf(0x55)),
        SMART_POSTER(byteArrayOf(0x53, 0x70)),
        ALTERNATIVE_CARRIER(byteArrayOf(0x61, 0x63)),
        HANDOVER_CARRIER(byteArrayOf(0x48, 0x63)),
        HANDOVER_REQUEST(byteArrayOf(0x68, 0x72)),
        HANDOVER_SELECT(byteArrayOf(0x64, 0x73)),
        ANDROID_APP("android.com:pkg".toByteArray());

        companion object {
            fun getByValue(value: ByteArray): RTD? =
                entries.firstOrNull { it.value.contentEquals(value) }
        }
    }
}
