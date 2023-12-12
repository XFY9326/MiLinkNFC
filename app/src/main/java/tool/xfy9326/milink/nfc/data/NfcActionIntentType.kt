package tool.xfy9326.milink.nfc.data

import androidx.annotation.StringRes
import tool.xfy9326.milink.nfc.R

enum class NfcActionIntentType(@StringRes val resId: Int) {
    FAKE_NFC_TAG(R.string.mirror_intent_fake_nfc_tag),
    MI_CONNECT_SERVICE(R.string.mirror_intent_mi_connect_service);
}