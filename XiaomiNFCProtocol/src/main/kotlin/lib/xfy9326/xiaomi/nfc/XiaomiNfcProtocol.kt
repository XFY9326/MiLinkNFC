package lib.xfy9326.xiaomi.nfc

sealed class XiaomiNfcProtocol<T : AppsData>(internal val flag: Byte) {
    companion object {
        private const val FLAG_V1 = 0.toByte()
        private const val FLAG_V2 = 1.toByte()
        private const val FLAG_HANDOFF = 3.toByte()

        fun parse(value: Byte) =
            when (value) {
                FLAG_V1 -> V1
                FLAG_V2 -> V2
                FLAG_HANDOFF -> HandOff
                else -> error("Unknown protocol flag $value")
            }
    }

    abstract fun decode(bytes: ByteArray): T

    data object V1 : XiaomiNfcProtocol<NfcTagAppData>(FLAG_V1) {
        override fun decode(bytes: ByteArray): NfcTagAppData = NfcTagAppData.decode(bytes)
    }

    data object V2 : XiaomiNfcProtocol<NfcTagAppData>(FLAG_V2) {
        override fun decode(bytes: ByteArray): NfcTagAppData = NfcTagAppData.decode(bytes)
    }

    data object HandOff : XiaomiNfcProtocol<HandoffAppData>(FLAG_HANDOFF) {
        override fun decode(bytes: ByteArray): HandoffAppData = HandoffAppData.decode(bytes)
    }
}