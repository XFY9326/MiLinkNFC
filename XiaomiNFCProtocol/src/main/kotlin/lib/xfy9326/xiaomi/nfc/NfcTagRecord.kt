package lib.xfy9326.xiaomi.nfc

import java.nio.ByteBuffer

sealed class NfcTagRecord(private val type: Byte) : BinaryData {
    companion object {
        internal const val TYPE_DEVICE = 0x01.toByte()
        internal const val TYPE_ACTION = 0x02.toByte()

        fun decode(buffer: ByteBuffer): NfcTagRecord {
            val type = buffer.get()
            val size = buffer.getShort() - Byte.SIZE_BYTES - Short.SIZE_BYTES
            val content = ByteArray(size).also { buffer.get(it) }
            return when (type) {
                TYPE_DEVICE -> content.toDeviceRecord()
                TYPE_ACTION -> content.toActionRecord()
                else -> error("Unknown NfcTagRecord type $type")
            }
        }

        private fun ByteArray.toDeviceRecord(): NfcTagDeviceRecord {
            val buffer = ByteBuffer.wrap(this)
            return NfcTagDeviceRecord(
                deviceType = buffer.getShort(),
                flags = buffer.get(),
                deviceNumber = buffer.get(),
                attributesMap = buffer.getShortKeyBytesMap()
            )
        }

        private fun ByteArray.toActionRecord(): NfcTagActionRecord {
            val buffer = ByteBuffer.wrap(this)
            return NfcTagActionRecord(
                action = buffer.getShort(),
                condition = buffer.get(),
                deviceNumber = buffer.get(),
                flags = buffer.get(),
                conditionParameters = if (buffer.hasRemaining()) ByteArray(buffer.remaining()).also {
                    buffer.get(
                        it
                    )
                } else null
            )
        }
    }

    protected abstract fun encodeContent(): ByteArray

    final override fun encode(): ByteArray {
        val content = encodeContent()
        val totalSize = Byte.SIZE_BYTES + // type
                Short.SIZE_BYTES + // content size
                content.size // content
        return ByteBuffer.allocate(totalSize)
            .put(type)
            .putShort(totalSize.toShort())
            .put(content)
            .array()
    }
}
