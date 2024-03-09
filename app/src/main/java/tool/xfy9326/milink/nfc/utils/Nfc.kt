package tool.xfy9326.milink.nfc.utils

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.TagTechnology
import androidx.core.os.bundleOf

private const val NFC_TAG_IGNORE_MILLS = 1000
private const val NFC_TAG_CHECK_DELAY_MILLS = 250

val EmptyNdefMessage = NdefMessage(NdefRecord(NdefRecord.TNF_EMPTY, null, null, null))

fun NfcAdapter.enableNdefReaderMode(activity: Activity, callBack: (Tag) -> Unit) {
    val flags = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V
    val options = bundleOf(
        NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY to NFC_TAG_CHECK_DELAY_MILLS
    )
    enableReaderMode(activity, callBack, flags, options)
}

fun NfcAdapter.requireEnabled(): Boolean = runCatching {
    isEnabled
}.recoverCatching {
    isEnabled
}.getOrDefault(false)

fun NfcAdapter.ignoreTagUntilRemoved(tag: Tag) =
    try {
        ignore(tag, NFC_TAG_IGNORE_MILLS, null, null)
    } catch (e: Exception) {
        e.printStackTrace()
    }

val Tag.techNameList: List<String>
    get() = techList.map { str -> str.substringAfterLast(".") }

fun <T : TagTechnology> T.requireConnect(): Boolean =
    try {
        if (!isConnected) connect()
        require(isConnected)
        true
    } catch (e: Exception) {
        false
    }

fun <T : TagTechnology> T.safeClose(): Boolean =
    try {
        close()
        true
    } catch (e: Exception) {
        false
    }

fun NdefMessage?.isNullOrEmpty(): Boolean =
    this == null || records.isEmpty() || records.all { it.tnf == NdefRecord.TNF_EMPTY }
