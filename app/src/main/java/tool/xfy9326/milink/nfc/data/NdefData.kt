package tool.xfy9326.milink.nfc.data

import android.nfc.NdefRecord
import tool.xfy9326.milink.nfc.utils.EMPTY

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

@Suppress("SpellCheckingInspection")
private val URI_PREFIX_MAP = arrayOf(
    "",  // 0x00
    "http://www.",  // 0x01
    "https://www.",  // 0x02
    "http://",  // 0x03
    "https://",  // 0x04
    "tel:",  // 0x05
    "mailto:",  // 0x06
    "ftp://anonymous:anonymous@",  // 0x07
    "ftp://ftp.",  // 0x08
    "ftps://",  // 0x09
    "sftp://",  // 0x0A
    "smb://",  // 0x0B
    "nfs://",  // 0x0C
    "ftp://",  // 0x0D
    "dav://",  // 0x0E
    "news:",  // 0x0F
    "telnet://",  // 0x10
    "imap:",  // 0x11
    "rtsp://",  // 0x12
    "urn:",  // 0x13
    "pop:",  // 0x14
    "sip:",  // 0x15
    "sips:",  // 0x16
    "tftp:",  // 0x17
    "btspp://",  // 0x18
    "btl2cap://",  // 0x19
    "btgoep://",  // 0x1A
    "tcpobex://",  // 0x1B
    "irdaobex://",  // 0x1C
    "file://",  // 0x1D
    "urn:epc:id:",  // 0x1E
    "urn:epc:tag:",  // 0x1F
    "urn:epc:pat:",  // 0x20
    "urn:epc:raw:",  // 0x21
    "urn:epc:",  // 0x22
    "urn:nfc:",  // 0x23
)

fun NdefRecord.getPayloadUri() =
    if (tnf == NdefRecord.TNF_WELL_KNOWN && type.contentEquals(NdefRecord.RTD_URI) && payload.isNotEmpty()) {
        runCatching {
            val prefix = URI_PREFIX_MAP[payload[0].toInt()]
            val uri = if (payload.size > 1) {
                payload.copyOfRange(1, payload.size).toString(Charsets.UTF_8)
            } else EMPTY
            prefix + uri
        }.getOrNull()
    } else null

fun NdefRecord.getText() = if (
    tnf == NdefRecord.TNF_WELL_KNOWN &&
    type.contentEquals(NdefRecord.RTD_TEXT) &&
    payload.size > 2
) {
    runCatching {
        val status = payload[0].toInt()
        if (payload.size > 1 + status) {
            val code = payload.copyOfRange(1, 1 + status).toString(Charsets.US_ASCII)
            val text = payload.copyOfRange(1 + status, payload.size)
                .toString(Charsets.UTF_8)
            code to text
        } else null
    }.getOrNull()
} else null