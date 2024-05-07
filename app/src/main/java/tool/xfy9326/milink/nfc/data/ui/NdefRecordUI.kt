package tool.xfy9326.milink.nfc.data.ui

import android.net.Uri
import lib.xfy9326.xiaomi.nfc.XiaomiNdefTNF
import tool.xfy9326.milink.nfc.data.NdefRTD
import tool.xfy9326.milink.nfc.data.NdefTNF

sealed interface NdefRecordUI {
    data class Default(
        val id: String?,
        val tnf: NdefTNF,
        val rtd: NdefRTD?,
        val typeText: String?,
        val typeHex: String?,
        val smartPosterUri: Uri?,
        val payloadLanguage: String?,
        private val payloadText: String?,
        private val payloadHex: String?
    ) : NdefRecordUI {
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

    data class XiaomiNfc(
        val ndefType: XiaomiNdefTNF,
        val payload: XiaomiNfcPayloadUI,
        val appData: AppDataUI,
    ) : NdefRecordUI
}