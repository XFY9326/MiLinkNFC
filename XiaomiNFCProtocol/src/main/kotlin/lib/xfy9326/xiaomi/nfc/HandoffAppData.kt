package lib.xfy9326.xiaomi.nfc

import java.nio.ByteBuffer

data class HandoffAppData(
    val majorVersion: Byte,
    val minorVersion: Byte,
    val deviceType: DeviceType,
    val attributesMap: Map<Byte, ByteArray>,
    val action: String,
    val payloadsMap: Map<PayloadKey, ByteArray>
) : AppsData {
    @Suppress("unused")
    companion object {
        fun decode(bytes: ByteArray): HandoffAppData {
            val buffer = ByteBuffer.wrap(bytes)
            return HandoffAppData(
                majorVersion = buffer.get(),
                minorVersion = buffer.get(),
                deviceType = DeviceType.parse(buffer.getInt()),
                attributesMap = buffer.getByteKeyBytesMap(buffer.get().toInt()),
                action = ByteArray(buffer.get().toInt()).also { buffer.get(it) }.toString(Charsets.UTF_8),
                payloadsMap = buffer.getByteKeyBytesMap().mapKeys { PayloadKey.parse(it.key) }
            )
        }

        fun encodePayloadsMap(map: Map<PayloadKey, ByteArray>): ByteArray {
            val byteMap = map.mapKeys { it.key.value }
            return ByteBuffer.allocate(byteMap.bytesMapTotalBytes())
                .putByteKeyBytesMap(byteMap)
                .array()
        }

        fun decodePayloadsMap(bytes: ByteArray): Map<PayloadKey, ByteArray> {
            val buffer = ByteBuffer.wrap(bytes)
            return buffer.getByteKeyBytesMap().mapKeys { PayloadKey.parse(it.key) }
        }
    }

    override fun encode(): ByteArray {
        val actionBytes = action.toByteArray(Charsets.UTF_8)
        val payloadByteMap = payloadsMap.mapKeys { it.key.value }
        return ByteBuffer.allocate(
            Byte.SIZE_BYTES + // major version
                    Byte.SIZE_BYTES + // minor version
                    Int.SIZE_BYTES + // deviceType
                    Byte.SIZE_BYTES + // attributeMap size
                    attributesMap.bytesMapTotalBytes() + // attributeMap
                    Byte.SIZE_BYTES + // action size
                    actionBytes.size + // action
                    payloadByteMap.bytesMapTotalBytes() // payloadData
        )
            .put(majorVersion)
            .put(minorVersion)
            .putInt(deviceType.value)
            .put(attributesMap.size.toByte())
            .putByteKeyBytesMap(attributesMap)
            .put(action.length.toByte())
            .put(action.toByteArray(Charsets.UTF_8))
            .putByteKeyBytesMap(payloadByteMap)
            .array()
    }

    enum class DeviceType(val value: Int) {
        UNKNOWN(0),
        TV(2),
        PC(3),
        CAR(5),
        PAD(8);

        companion object {
            private val valuesMap by lazy { entries.associateBy { it.value } }

            fun parse(value: Int) = valuesMap[value] ?: UNKNOWN
        }
    }

    enum class PayloadKey(val value: Byte) {
        UNKNOWN(0),
        ACTION_SUFFIX(101),
        BT_MAC(1),
        WIFI_MAC(2),
        WIRED_MAC(3),
        EXT_ABILITY(121);

        companion object {
            private val valuesMap by lazy { entries.associateBy { it.value } }

            fun parse(value: Byte) = valuesMap[value] ?: UNKNOWN
        }
    }
}