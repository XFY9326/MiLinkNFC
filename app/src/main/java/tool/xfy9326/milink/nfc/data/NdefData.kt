package tool.xfy9326.milink.nfc.data

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
        private val lazyMap by lazy { entries.associateBy { it.value } }
        fun getByValue(value: Byte): NdefTNF = lazyMap[value] ?: UNKNOWN
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
        private val lazyMap by lazy { entries.associateBy { it.value } }

        fun getByValue(value: ByteArray): NdefRTD? = lazyMap[value]
    }
}