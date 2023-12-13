package lib.xfy9326.xiaomi.nfc

import java.nio.ByteBuffer

data class HandoffAppData(
    val majorVersion: Byte,
    val minorVersion: Byte,
    val deviceType: Int,
    val attributesMap: Map<Byte, ByteArray>,
    val action: String,
    val payloadsMap: Map<Byte, ByteArray>
) : AppsData {
    @Suppress("unused")
    companion object {
        fun newInstance(
            majorVersion: Byte,
            minorVersion: Byte,
            deviceType: DeviceType,
            attributesMap: Map<Byte, ByteArray>,
            action: String,
            payloadsMap: Map<PayloadKey, ByteArray>
        ) = HandoffAppData(
            majorVersion = majorVersion,
            minorVersion = minorVersion,
            deviceType = deviceType.value,
            attributesMap = attributesMap,
            action = action,
            payloadsMap = payloadsMap.mapKeys { it.key.value }
        )

        fun decode(bytes: ByteArray): HandoffAppData {
            val buffer = ByteBuffer.wrap(bytes)
            return HandoffAppData(
                majorVersion = buffer.get(),
                minorVersion = buffer.get(),
                deviceType = buffer.getInt(),
                attributesMap = buffer.getByteKeyBytesMap(buffer.get().toInt()),
                action = ByteArray(buffer.get().toInt()).also { buffer.get(it) }.toString(Charsets.UTF_8),
                payloadsMap = buffer.getByteKeyBytesMap()
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

    val enumDeviceType by lazy { DeviceType.parse(deviceType) }
    val enumPayloadsMap
        get() = payloadsMap.mapKeys { PayloadKey.parse(it.key) }

    override fun encode(): ByteArray {
        val actionBytes = action.toByteArray(Charsets.UTF_8)
        return ByteBuffer.allocate(
            Byte.SIZE_BYTES + // major version
                    Byte.SIZE_BYTES + // minor version
                    Int.SIZE_BYTES + // deviceType
                    Byte.SIZE_BYTES + // attributeMap size
                    attributesMap.bytesMapTotalBytes() + // attributeMap
                    Byte.SIZE_BYTES + // action size
                    actionBytes.size + // action
                    payloadsMap.bytesMapTotalBytes() // payloadData
        )
            .put(majorVersion)
            .put(minorVersion)
            .putInt(deviceType)
            .put(attributesMap.size.toByte())
            .putByteKeyBytesMap(attributesMap)
            .put(action.length.toByte())
            .put(action.toByteArray(Charsets.UTF_8))
            .putByteKeyBytesMap(payloadsMap)
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

    enum class PayloadKey(val value: Byte, val isText: Boolean? = null) {
        UNKNOWN(0),
        ACTION_SUFFIX(101, true),
        BT_MAC(1, true),
        WIFI_MAC(2, true),
        WIRED_MAC(3, true),
        EXT_ABILITY(121, false);

        companion object {
            private val valuesMap by lazy { entries.associateBy { it.value } }

            fun parse(value: Byte) = valuesMap[value] ?: UNKNOWN
        }
    }
}