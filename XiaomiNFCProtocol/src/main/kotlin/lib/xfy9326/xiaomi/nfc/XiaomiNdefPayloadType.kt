package lib.xfy9326.xiaomi.nfc

enum class XiaomiNdefPayloadType(val value: String) {
    UNKNOWN(""),
    SMART_HOME("com.xiaomi.smarthome:externaltype"),
    MI_CONNECT_SERVICE("com.xiaomi.mi_connect_service:externaltype");

    val bytes by lazy {
        value.toByteArray(Charsets.US_ASCII)
    }

    companion object {
        private val valuesMap by lazy { entries.associateBy { it.value } }

        fun parse(value: String) = valuesMap[value] ?: UNKNOWN
    }
}