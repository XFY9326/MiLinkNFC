package tool.xfy9326.milink.nfc.data.ui

import android.net.Uri
import lib.xfy9326.xiaomi.nfc.XiaomiNdefTNF
import tool.xfy9326.milink.nfc.data.NdefRTD
import tool.xfy9326.milink.nfc.data.NdefTNF

sealed interface NdefRecordUI {
    data class Default(
        val id: String?,
        val tnf: NdefTNF,
        private val rtdType: NdefRTD?,
        private val rtdText: String?,
        val rtdHex: String?,
        private val mimeType: String?,
        private val uri: Uri?,
        private val payload: String?
    ) : NdefRecordUI {
        val payloadText: String?
            get() = uri?.toString() ?: payload

        val rtd: String?
            get() = mimeType ?: rtdType?.name ?: rtdText
    }

    data class XiaomiNfc(
        val ndefType: XiaomiNdefTNF,
        val payload: XiaomiNfcPayloadUI,
        val appData: AppDataUI,
    ) : NdefRecordUI
}