package tool.xfy9326.milink.nfc.protocol

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import androidx.core.content.IntentCompat
import tool.xfy9326.milink.nfc.utils.toHexString

object HuaweiHandoffNfc {
    private const val BT_MAC_START_IDX = 2
    private const val BT_MAC_END_IDX = 8

    @Suppress("SpellCheckingInspection")
    private const val MIME_NDEF_MSG = "application/vnd.huawei.handoff.ndefmsg"
    const val NFC_URI_CONTENT = "consumer.huawei.com/en/support/huaweisharewelcome/"

    fun parseBluetoothMac(intent: Intent): String? =
        IntentCompat.getParcelableArrayExtra(intent, NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)
            ?.filterIsInstance<NdefMessage>()?.asSequence()?.flatMap {
                it.records.asSequence()
            }?.filterNotNull()?.find {
                it.toMimeType() == MIME_NDEF_MSG
            }?.payload?.let(this::getBluetoothMac)?.toHexString(true)

    private fun getBluetoothMac(payload: ByteArray) =
        payload.copyOfRange(BT_MAC_START_IDX, BT_MAC_END_IDX)
}