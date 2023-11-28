package tool.xfy9326.milink.nfc.utils

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.core.os.bundleOf

private const val NFC_TAG_IGNORE_MILLS = 1000

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

fun NfcAdapter.ignoreTagUntilRemoved(tag: Tag) {
    ignore(tag, NFC_TAG_IGNORE_MILLS, null, null)
}
