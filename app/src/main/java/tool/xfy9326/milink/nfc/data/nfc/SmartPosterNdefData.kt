package tool.xfy9326.milink.nfc.data.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import androidx.annotation.StringRes
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.utils.toHexText

data class SmartPosterNdefData(
    val uri: android.net.Uri,
    val items: List<NdefData>
) : NdefData {
    companion object {
        private val ACTION_RECORD_TYPE = "act".toByteArray()
        private val SIZE_RECORD_TYPE = "s".toByteArray()
        private val TYPE_RECORD_TYPE = "t".toByteArray()

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
                    record.toMimeType()?.let {
                        it.startsWith("image/") || it.startsWith("video/")
                    } == true

        private fun checkSizeType(record: NdefRecord): Boolean =
            record.type.contentEquals(SIZE_RECORD_TYPE)

        private fun checkTypeType(record: NdefRecord): Boolean =
            record.type.contentEquals(TYPE_RECORD_TYPE)

        private fun parseSubRecords(record: NdefRecord): MetaData? =
            if (checkTitleType(record)) {
                val rtdText = NdefData.parseRTDText(record.payload) ?: error("RTD Text error")
                Title(rtdText.first, rtdText.second)
            } else if (checkUriType(record)) {
                Uri(record.toUri() ?: error("Uri is empty"))
            } else if (checkActionType(record)) {
                Action(ActionType.getByValue(record.payload[0]))
            } else if (checkIconType(record)) {
                Icon(record.payload.toHexText())
            } else if (checkSizeType(record)) {
                Size(record.payload.getUIntAt(0).toInt())
            } else if (checkTypeType(record)) {
                Type(record.payload.toString(Charsets.UTF_8))
            } else null

        fun parse(record: NdefRecord): SmartPosterNdefData =
            SmartPosterNdefData(
                uri = record.toUri() ?: error("Uri is empty"),
                items = NdefMessage(record.payload).records.asSequence().filterNotNull().map {
                    parseSubRecords(it) ?: NdefData.parseWellKnown(it, true)
                }.toList()
            )

        fun sortFields(field: List<MetaData>) = field.sortedBy { it.displayOrder }
    }

    sealed interface MetaData : NdefData {
        val displayOrder: Int
    }

    enum class ActionType(val value: Byte, @StringRes val resId: Int) {
        DO_THE_ACTION(0x00, R.string.ndef_sp_action_do_the_action),
        SAVE_FOR_LATER(0x01, R.string.ndef_sp_action_save_for_later),
        OPEN_FOR_EDITING(0x02, R.string.ndef_sp_action_open_for_editing),
        UNDEFINED(Byte.MIN_VALUE, R.string.ndef_sp_action_undefined);

        companion object {
            fun getByValue(value: Byte): ActionType =
                entries.firstOrNull { it.value == value } ?: UNDEFINED
        }
    }

    data class Title(val languageCode: String, val text: String) : MetaData {
        override val displayOrder: Int = 0
    }

    data class Uri(val uri: android.net.Uri) : MetaData {
        override val displayOrder: Int = 1
    }

    data class Action(val actionType: ActionType) : MetaData {
        override val displayOrder: Int = 2
    }

    data class Icon(val payloadHex: String) : MetaData {
        override val displayOrder: Int = 3
    }

    data class Size(val value: Int) : MetaData {
        override val displayOrder: Int = 4
    }

    data class Type(val mimeType: String) : MetaData {
        override val displayOrder: Int = 5
    }
}