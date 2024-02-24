package lib.xfy9326.xiaomi.nfc

import java.nio.ByteBuffer

abstract class BinaryData {
    abstract fun size(): Int

    abstract fun encodeInto(buffer: ByteBuffer)

    fun encode(): ByteArray =
        ByteBuffer.allocate(size()).apply {
            encodeInto(this)
        }.array()
}