package lib.xfy9326.xiaomi.nfc

enum class XiaomiNdefPayloadType(val value: String) {
    SMART_HOME("com.xiaomi.smarthome:externaltype"),
    MI_CONNECT_SERVICE("com.xiaomi.mi_connect_service:externaltype");

    companion object {
        private val valuesMap by lazy { entries.associateBy { it.value } }

        fun parse(bytes: ByteArray): XiaomiNdefPayloadType = valuesMap.getValue(bytes.toString(Charsets.US_ASCII))
    }
}