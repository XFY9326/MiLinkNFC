package tool.xfy9326.milink.nfc.utils

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.TagTechnology
import androidx.core.os.bundleOf

private const val NFC_TAG_IGNORE_MILLS = 1000

val EmptyNdefMessage = NdefMessage(NdefRecord(NdefRecord.TNF_EMPTY, null, null, null))

fun NfcAdapter.enableNdefReaderMode(activity: Activity, callBack: (Tag) -> Unit) {
    val flags = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V
    val options = bundleOf(
        NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY to 250
    )
    enableReaderMode(activity, callBack, flags, options)
}

fun NfcAdapter.ignoreTagUntilRemoved(tag: Tag) =
    runCatching {
        ignore(tag, NFC_TAG_IGNORE_MILLS, null, null)
    }.getOrDefault(false)

fun <T : TagTechnology> T.tryConnect(): Result<T> = runCatching {
    if (!isConnected) connect()
    require(isConnected)
    this
}
