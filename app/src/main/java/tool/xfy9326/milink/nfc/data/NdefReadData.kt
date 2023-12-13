package tool.xfy9326.milink.nfc.data

import android.nfc.NdefMessage
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NdefReadData(
    val scanTime: Long,
    val techList: List<String>,
    val type: String,
    val msg: NdefMessage,
    val maxSize: Int,
    val writeable: Boolean,
    val canMakeReadOnly: Boolean
) : Parcelable