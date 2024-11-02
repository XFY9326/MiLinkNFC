package tool.xfy9326.milink.nfc.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.TagTechnology
import androidx.core.content.IntentCompat

private const val NFC_TAG_IGNORE_MILLS = 2000

val EmptyNdefMessage = NdefMessage(NdefRecord(NdefRecord.TNF_EMPTY, null, null, null))

fun NfcAdapter.enableNdefForegroundDispatch(
    activity: Activity,
    allowNdefFormatable: Boolean = false
) {
    val intent = Intent(activity, activity.javaClass).apply {
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_MUTABLE)
    val intentFiltersArray = arrayOf(
        if (allowNdefFormatable) {
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        } else {
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        }
    )
    val techListsArray = arrayOf(
        if (allowNdefFormatable) {
            arrayOf<String>(Ndef::class.java.name, NdefFormatable::class.java.name)
        } else {
            arrayOf<String>(Ndef::class.java.name)
        }
    )
    enableForegroundDispatch(activity, pendingIntent, intentFiltersArray, techListsArray)
}

fun Intent.tryGetNfcTag(): Tag? = takeIf {
    it.action == NfcAdapter.ACTION_TAG_DISCOVERED || it.action == NfcAdapter.ACTION_NDEF_DISCOVERED || it.action == NfcAdapter.ACTION_TECH_DISCOVERED
}?.let {
    IntentCompat.getParcelableExtra(it, NfcAdapter.EXTRA_TAG, Tag::class.java)
}

fun NfcAdapter.requireEnabled(): Boolean = runCatching {
    isEnabled
}.recoverCatching {
    isEnabled
}.getOrDefault(false)

fun Context.ignoreTagUntilRemoved(tag: Tag): Boolean =
    runCatching {
        NfcAdapter.getDefaultAdapter(this)?.ignore(tag, NFC_TAG_IGNORE_MILLS, null, null)
    }.getOrNull() == true

val Tag.techNameList: List<String>
    get() = techList.map { str -> str.substringAfterLast(".") }

fun <T : TagTechnology> T.requireConnect(): Boolean =
    runCatching {
        if (!isConnected) connect()
        require(isConnected)
        true
    }.getOrDefault(false)

fun <T : TagTechnology> T.safeClose(): Boolean =
    runCatching {
        close()
        true
    }.getOrDefault(false)

fun NdefMessage?.isNullOrEmpty(): Boolean =
    this == null || records.isEmpty() || records.all { it.tnf == NdefRecord.TNF_EMPTY }
