package lib.xfy9326.xiaomi.nfc

interface BinaryData {
    fun size(): Int

    fun encode(): ByteArray
}