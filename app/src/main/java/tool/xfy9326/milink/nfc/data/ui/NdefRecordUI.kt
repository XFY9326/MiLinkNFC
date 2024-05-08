package tool.xfy9326.milink.nfc.data.ui

import android.content.Intent
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import lib.xfy9326.xiaomi.nfc.HandoffAppData
import lib.xfy9326.xiaomi.nfc.NfcTagAppData
import lib.xfy9326.xiaomi.nfc.XiaomiNdefType
import lib.xfy9326.xiaomi.nfc.XiaomiNfcPayload
import tool.xfy9326.milink.nfc.data.NdefRTD
import tool.xfy9326.milink.nfc.data.NdefTNF
import tool.xfy9326.milink.nfc.utils.toHexText

sealed interface NdefRecordUI {

    sealed interface SmartPosterItem

    sealed interface SmartPosterField : SmartPosterItem {
        val priority: Int
    }

    companion object {
        private fun parseRTDText(bytes: ByteArray): Pair<String, String>? = runCatching {
            val status = bytes[0].toInt()
            if (bytes.size > 1 + status) {
                val code = bytes.copyOfRange(1, 1 + status).toString(Charsets.US_ASCII)
                val text = bytes.copyOfRange(1 + status, bytes.size)
                    .toString(Charsets.UTF_8)
                code to text
            } else null
        }.getOrNull()

        private fun NdefRecord.getRTDText() =
            if (
                tnf == NdefRecord.TNF_WELL_KNOWN &&
                type.contentEquals(NdefRecord.RTD_TEXT) &&
                payload.size > 2
            ) parseRTDText(payload) else null
    }

    data class Simple(
        val id: String?,
        val tnf: NdefTNF,
        val rtd: NdefRTD?,
        val typeText: String?,
        val typeHex: String?,
        val payloadLanguage: String?,
        private val payloadText: String?,
        private val payloadHex: String?
    ) : NdefRecordUI, SmartPosterItem {
        companion object {
            fun parse(record: NdefRecord): Simple {
                val payloadRTDText = record.getRTDText()
                return Simple(
                    id = record.id?.takeIf { it.isNotEmpty() }?.toHexText(),
                    tnf = NdefTNF.getByValue(record.tnf.toByte()),
                    rtd = NdefRTD.getByValue(record.type),
                    typeText = record.type.takeIf { it.isNotEmpty() }?.runCatching {
                        when (record.tnf) {
                            NdefRecord.TNF_ABSOLUTE_URI ->
                                Uri.parse(toString(Charsets.UTF_8)).normalizeScheme().toString()

                            NdefRecord.TNF_MIME_MEDIA ->
                                Intent.normalizeMimeType(toString(Charsets.US_ASCII))

                            else -> null
                        }
                    }?.getOrNull(),
                    typeHex = record.type?.takeIf { it.isNotEmpty() }?.toHexText(),
                    payloadLanguage = payloadRTDText?.first,
                    payloadText = record.payload?.takeIf { it.isNotEmpty() }?.runCatching {
                        if (
                            record.tnf == NdefRecord.TNF_EXTERNAL_TYPE &&
                            record.type.contentEquals(NdefRTD.ANDROID_APP.value)
                        ) {
                            record.payload.toString(Charsets.UTF_8)
                        } else if (
                            record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                            record.type.contentEquals(NdefRecord.RTD_TEXT)
                        ) {
                            payloadRTDText?.second
                        } else if (
                            record.tnf == NdefRecord.TNF_MIME_MEDIA &&
                            record.type.contentEquals("text/plain".toByteArray())
                        ) {
                            record.payload.toString(Charsets.UTF_8)
                        } else if (
                            record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                            record.type.contentEquals(NdefRecord.RTD_URI)
                        ) {
                            record.toUri()?.toString()
                        } else null
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

    data class SmartPoster(val uri: android.net.Uri, val items: List<SmartPosterItem>) :
        NdefRecordUI {
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

            private fun ByteArray.getUIntAt(idx: Int) =
                ((this[idx].toUInt() and 0xFFu) shl 24) or
                        ((this[idx + 1].toUInt() and 0xFFu) shl 16) or
                        ((this[idx + 2].toUInt() and 0xFFu) shl 8) or
                        (this[idx + 3].toUInt() and 0xFFu)

            fun checkType(record: NdefRecord): Boolean =
                record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                        record.type.contentEquals(NdefRecord.RTD_SMART_POSTER)

            private fun checkTitleType(record: NdefRecord): Boolean =
                record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                        record.type.contentEquals(NdefRecord.RTD_TEXT)

            private fun checkUriType(record: NdefRecord): Boolean =
                record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                        record.type.contentEquals(NdefRecord.RTD_URI) ||
                        record.tnf == NdefRecord.TNF_ABSOLUTE_URI

            private fun checkActionType(record: NdefRecord): Boolean =
                record.type.contentEquals(ACTION_RECORD_TYPE)

            private fun checkIconType(record: NdefRecord): Boolean =
                record.tnf == NdefRecord.TNF_MIME_MEDIA &&
                        (record.type.startsWith("image/".toByteArray()) ||
                                record.type.startsWith("video/".toByteArray()))

            private fun checkSizeType(record: NdefRecord): Boolean =
                record.type.contentEquals(SIZE_RECORD_TYPE)

            private fun checkTypeType(record: NdefRecord): Boolean =
                record.type.contentEquals(TYPE_RECORD_TYPE)

            private fun parseSubRecords(record: NdefRecord): SmartPosterItem? =
                if (checkTitleType(record)) {
                    val rtdText = parseRTDText(record.payload)!!
                    Title(rtdText.first, rtdText.second)
                } else if (checkUriType(record)) {
                    Uri(record.toUri()!!)
                } else if (checkActionType(record)) {
                    Action(ActionType.getByValue(record.payload[0]))
                } else if (checkIconType(record)) {
                    Icon(record.payload.toHexText())
                } else if (checkSizeType(record)) {
                    Size(record.payload.getUIntAt(0).toInt())
                } else if (checkTypeType(record)) {
                    Type(record.payload.toString(Charsets.UTF_8))
                } else null

            fun parse(record: NdefRecord): SmartPoster =
                NdefMessage(record.payload).records.asSequence().filterNotNull().map {
                    parseSubRecords(it) ?: Simple.parse(it)
                }.toList().let {
                    SmartPoster(record.toUri()!!, it)
                }

            fun sortFields(field: List<SmartPosterField>) = field.sortedBy { it.priority }
        }

        data class Title(val languageCode: String, val text: String) : SmartPosterField {
            override val priority: Int = 0
        }

        data class Uri(val uri: android.net.Uri) : SmartPosterField {
            override val priority: Int = 1
        }

        data class Action(val actionType: ActionType) : SmartPosterField {
            override val priority: Int = 2
        }

        data class Icon(val payloadHex: String) : SmartPosterField {
            override val priority: Int = 3
        }

        data class Size(val value: Int) : SmartPosterField {
            override val priority: Int = 4
        }

        data class Type(val mimeType: String) : SmartPosterField {
            override val priority: Int = 5
        }
    }

    data class Xiaomi(
        val ndefType: XiaomiNdefType,
        val payload: XiaomiNfcPayloadUI,
        val appData: AppDataUI,
    ) : NdefRecordUI {
        companion object {
            fun parse(ndefType: XiaomiNdefType, payload: XiaomiNfcPayload<*>): Xiaomi =
                Xiaomi(
                    ndefType = ndefType,
                    payload = XiaomiNfcPayloadUI(payload),
                    appData = when (val appData = payload.appData) {
                        is HandoffAppData -> HandoffAppDataUI(appData)
                        is NfcTagAppData -> NfcTagAppDataUI(appData, ndefType)
                    }
                )
        }
    }
}