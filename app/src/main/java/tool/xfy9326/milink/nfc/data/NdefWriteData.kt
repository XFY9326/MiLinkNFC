package tool.xfy9326.milink.nfc.data

import android.nfc.NdefMessage
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NdefWriteData(
    val ndefMsg: NdefMessage,
    val readOnly: Boolean
) : Parcelable