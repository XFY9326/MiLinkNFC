package lib.xfy9326.xiaomi.nfc

import java.nio.ByteBuffer

sealed class NfcTagRecord(val type: Byte) : BinaryData {
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
                deviceType = NfcTagDeviceRecord.DeviceType.parse(buffer.getShort()),
                flags = buffer.get(),
                deviceNumber = buffer.get(),
                attributesMap = buffer.getShortKeyBytesMap().mapKeys { NfcTagDeviceRecord.DeviceAttribute.parse(it.key) }
            )
        }

        private fun ByteArray.toActionRecord(): NfcTagActionRecord {
            val buffer = ByteBuffer.wrap(this)
            return NfcTagActionRecord(
                action = NfcTagActionRecord.Action.parse(buffer.getShort()),
                condition = NfcTagActionRecord.Condition.parse(buffer.get()),
                deviceNumber = buffer.get(),
                flags = buffer.get(),
                conditionParameters = ByteArray(buffer.remaining()).also { buffer.get(it) }
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
