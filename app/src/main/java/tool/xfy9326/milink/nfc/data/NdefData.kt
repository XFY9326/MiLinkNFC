package tool.xfy9326.milink.nfc.data

import android.nfc.NdefMessage
import android.nfc.NdefRecord

enum class NdefTNF(val value: Byte) {
    EMPTY(0x00),
    WELL_KNOWN(0x01),
    MIME_MEDIA(0x02),
    ABSOLUTE_URI(0x03),
    EXTERNAL_TYPE(0x04),
    UNKNOWN(0x05),
    UNCHANGED(0x06),
    RESERVED(0x07);

    companion object {
        fun getByValue(value: Byte): NdefTNF = entries.firstOrNull { it.value == value } ?: UNKNOWN
    }
}

enum class NdefRTD(val value: ByteArray) {
    TEXT(byteArrayOf(0x54)),
    URI(byteArrayOf(0x55)),
    SMART_POSTER(byteArrayOf(0x53, 0x70)),
    ALTERNATIVE_CARRIER(byteArrayOf(0x61, 0x63)),
    HANDOVER_CARRIER(byteArrayOf(0x48, 0x63)),
    HANDOVER_REQUEST(byteArrayOf(0x68, 0x72)),
    HANDOVER_SELECT(byteArrayOf(0x64, 0x73)),
    ANDROID_APP("android.com:pkg".toByteArray());

    companion object {
        fun getByValue(value: ByteArray): NdefRTD? =
            entries.firstOrNull { it.value.contentEquals(value) }
    }
}

sealed interface NdefSmartPoster {
    enum class ActionType(val value: Byte) {
        DO_THE_ACTION(0x00),
        SAVE_FOR_LATER(0x01),
        OPEN_FOR_EDITING(0x02),
        UNDEFINED(Byte.MIN_VALUE);

        companion object {
            fun getByValue(value: Byte): ActionType =
                entries.firstOrNull { it.value == value } ?: UNDEFINED
        }
    }

    companion object {
        private val ACTION_RECORD_TYPE = "act".toByteArray()
        private val SIZE_RECORD_TYPE = "s".toByteArray()
        private val TYPE_RECORD_TYPE = "t".toByteArray()

        private fun ByteArray.startsWith(byteArray: ByteArray): Boolean {
            if (this.size < byteArray.size) return false
            for (i in indices) {
                if (this[i] != byteArray[i]) return false
            }
            return true
        }
    }

    data class Title(val language: String, val text: String) : NdefSmartPoster {
        companion object {
            fun checkType(record: NdefRecord): Boolean =
                record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                        record.type.contentEquals(NdefRecord.RTD_TEXT)
        }
    }

    data class Uri(val uri: android.net.Uri) : NdefSmartPoster {
        companion object {
            fun checkType(record: NdefRecord): Boolean =
                record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                        record.type.contentEquals(NdefRecord.RTD_URI) ||
                        record.tnf == NdefRecord.TNF_ABSOLUTE_URI
        }
    }

    data class Action(val actionType: ActionType) : NdefSmartPoster {
        companion object {
            fun checkType(record: NdefRecord): Boolean =
                record.type.contentEquals(ACTION_RECORD_TYPE)
        }
    }

    data class Icon(val payload: ByteArray) : NdefSmartPoster {
        companion object {
            fun checkType(record: NdefRecord): Boolean =
                record.tnf == NdefRecord.TNF_MIME_MEDIA &&
                        (record.type.startsWith("image/".toByteArray()) ||
                                record.type.startsWith("video/".toByteArray()))
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Icon

            return payload.contentEquals(other.payload)
        }

        override fun hashCode(): Int {
            return payload.contentHashCode()
        }
    }

    data class Size(val value: Int) : NdefSmartPoster {
        companion object {
            fun checkType(record: NdefRecord): Boolean =
                record.type.contentEquals(SIZE_RECORD_TYPE)
        }
    }

    data class Type(val mimeType: String) : NdefSmartPoster {
        companion object {
            fun checkType(record: NdefRecord): Boolean =
                record.type.contentEquals(TYPE_RECORD_TYPE)
        }
    }
}

private fun parseRTDText(bytes: ByteArray): Pair<String, String>? = runCatching {
    val status = bytes[0].toInt()
    if (bytes.size > 1 + status) {
        val code = bytes.copyOfRange(1, 1 + status).toString(Charsets.US_ASCII)
        val text = bytes.copyOfRange(1 + status, bytes.size)
            .toString(Charsets.UTF_8)
        code to text
    } else null
}.getOrNull()

fun NdefRecord.getRTDText() =
    if (
        tnf == NdefRecord.TNF_WELL_KNOWN &&
        type.contentEquals(NdefRecord.RTD_TEXT) &&
        payload.size > 2
    ) parseRTDText(payload) else null

private fun ByteArray.getUIntAt(idx: Int) =
    ((this[idx].toUInt() and 0xFFu) shl 24) or
            ((this[idx + 1].toUInt() and 0xFFu) shl 16) or
            ((this[idx + 2].toUInt() and 0xFFu) shl 8) or
            (this[idx + 3].toUInt() and 0xFFu)

private fun parseSubSmartPosterRecords(record: NdefRecord): NdefSmartPoster? =
    if (NdefSmartPoster.Title.checkType(record)) {
        val rtdText = parseRTDText(record.payload)!!
        NdefSmartPoster.Title(rtdText.first, rtdText.second)
    } else if (NdefSmartPoster.Uri.checkType(record)) {
        NdefSmartPoster.Uri(record.toUri()!!)
    } else if (NdefSmartPoster.Action.checkType(record)) {
        NdefSmartPoster.Action(NdefSmartPoster.ActionType.getByValue(record.payload[0]))
    } else if (NdefSmartPoster.Icon.checkType(record)) {
        NdefSmartPoster.Icon(record.payload)
    } else if (NdefSmartPoster.Size.checkType(record)) {
        NdefSmartPoster.Size(record.payload.getUIntAt(0).toInt())
    } else if (NdefSmartPoster.Type.checkType(record)) {
        NdefSmartPoster.Type(record.payload.toString(Charsets.UTF_8))
    } else null

fun NdefRecord.getRTDSmartPoster() =
    if (
        tnf == NdefRecord.TNF_WELL_KNOWN &&
        type.contentEquals(NdefRecord.RTD_SMART_POSTER) &&
        payload.isNotEmpty()
    ) {
        runCatching {
            NdefMessage(payload).records.asSequence().filterNotNull().mapNotNull {
                parseSubSmartPosterRecords(it)
            }
        }.getOrNull()
    } else null
