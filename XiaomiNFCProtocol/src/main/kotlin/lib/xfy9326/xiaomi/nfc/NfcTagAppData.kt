package lib.xfy9326.xiaomi.nfc

import java.nio.ByteBuffer

@Suppress("unused")
data class NfcTagAppData(
    val majorVersion: Byte,
    val minorVersion: Byte,
    val writeTime: Int,
    val flags: Byte,
    val records: List<NfcTagRecord>
) : AppsData {
    companion object {
        fun decode(bytes: ByteArray): NfcTagAppData {
            val buffer = ByteBuffer.wrap(bytes)
            return NfcTagAppData(
                majorVersion = buffer.get(),
                minorVersion = buffer.get(),
                writeTime = buffer.getInt(),
                flags = buffer.get(),
                records = buildList {
                    repeat(buffer.get().toInt()) {
                        add(NfcTagRecord.decode(buffer))
                    }
                }
            )
        }
    }

    fun firstOrNullDeviceRecord(): NfcTagDeviceRecord? =
        records.asSequence().filterIsInstance<NfcTagDeviceRecord>().firstOrNull()

    fun firstOrNullActionRecord(): NfcTagActionRecord? =
        records.asSequence().filterIsInstance<NfcTagActionRecord>().firstOrNull()

    fun firstAction(): NfcTagActionRecord.Action =
        firstOrNullActionRecord()?.enumAction ?: NfcTagActionRecord.Action.UNKNOWN

    fun firstOrNullActionValue(): Short? =
        firstOrNullActionRecord()?.action

    fun getFirstDeviceAttributesMap(ndefType: XiaomiNdefPayloadType): Map<NfcTagDeviceRecord.DeviceAttribute, ByteArray> =
        firstOrNullDeviceRecord()?.getAllAttributesMap(firstAction(), ndefType) ?: emptyMap()

    override fun encode(): ByteArray {
        val recordByteArrays = records.map { it.encode() }
        return ByteBuffer.allocate(
            Byte.SIZE_BYTES + // majorVersion
                    Byte.SIZE_BYTES + // minorVersion
                    Int.SIZE_BYTES + // writeTime
                    Byte.SIZE_BYTES + // flags
                    Byte.SIZE_BYTES + // records size
                    recordByteArrays.totalBytes() // records
        )
            .put(majorVersion)
            .put(minorVersion)
            .putInt(writeTime)
            .put(flags)
            .put(records.size.toByte())
            .putByteArrays(recordByteArrays)
            .array()
    }
}
