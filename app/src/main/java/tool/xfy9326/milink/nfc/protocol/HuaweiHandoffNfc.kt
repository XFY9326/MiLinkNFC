package tool.xfy9326.milink.nfc.protocol

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import androidx.core.content.IntentCompat
import tool.xfy9326.milink.nfc.utils.toHexString

object HuaweiHandoffNfc {
    private const val BLUETOOTH_MAC_START_IDX = 2
    private const val BLUETOOTH_MAC_END_IDX = 8

    private const val MIME_NDEF_MSG = "application/vnd.huawei.handoff.ndefmsg"
    private const val NFC_HUAWEI_URI_CONTENT = "consumer.huawei.com/en/support/huaweisharewelcome/"
    private const val NFC_HONOR_URI_CONTENT = "www.honor.cn/support/magic_link/"

    fun parseBluetoothMac(intent: Intent): String? =
        IntentCompat.getParcelableArrayExtra(intent, NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)
            ?.asSequence()?.filterIsInstance<NdefMessage>()?.flatMap {
                it.records.asSequence()
            }?.filterNotNull()?.firstOrNull {
                it.toMimeType() == MIME_NDEF_MSG
            }?.payload?.let(this::getBluetoothMac)?.toHexString(true)

    fun hasNfcUriContent(text: String): Boolean {
        return NFC_HUAWEI_URI_CONTENT in text || NFC_HONOR_URI_CONTENT in text
    }

    private fun getBluetoothMac(payload: ByteArray): ByteArray? = runCatching {
        payload.copyOfRange(BLUETOOTH_MAC_START_IDX, BLUETOOTH_MAC_END_IDX)
    }.getOrNull()
}