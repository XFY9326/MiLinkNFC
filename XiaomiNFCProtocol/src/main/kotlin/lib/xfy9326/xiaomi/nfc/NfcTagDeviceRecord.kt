package lib.xfy9326.xiaomi.nfc

import java.nio.ByteBuffer

@Suppress("unused")
data class NfcTagDeviceRecord(
    val deviceType: DeviceType,
    val flags: Byte,
    val deviceNumber: Byte,
    val attributesMap: Map<DeviceAttribute, ByteArray>,
) : NfcTagRecord(TYPE_DEVICE) {

    companion object {
        private const val PREFIX_ATTR_APP_DATA = "mxD"

        fun isAppDataValueMap(bytes: ByteArray): Boolean {
            val prefix = PREFIX_ATTR_APP_DATA.toByteArray(Charsets.UTF_8)
            return bytes.size >= prefix.size && bytes.take(prefix.size) == prefix.asList()
        }

        fun encodeAppDataValueMap(map: Map<DeviceAttribute, ByteArray>): ByteArray {
            val prefix = PREFIX_ATTR_APP_DATA.toByteArray(Charsets.UTF_8)
            val attributeShortMap = map.mapKeys { it.key.value }
            return ByteBuffer.allocate(prefix.size + attributeShortMap.shortMapTotalBytes())
                .put(prefix)
                .putShortKeyBytesMap(attributeShortMap)
                .array()
        }

        fun decodeAppDataValueMap(bytes: ByteArray): Map<DeviceAttribute, ByteArray> {
            val prefix = PREFIX_ATTR_APP_DATA.toByteArray(Charsets.UTF_8)
            val buffer = ByteBuffer.wrap(bytes)
            require(ByteArray(prefix.size) { buffer.get(it) }.contentEquals(prefix)) { "Not an valid DeviceAttribute.APP_DATA byte array" }
            return buffer.getShortKeyBytesMap().mapKeys { DeviceAttribute.parse(it.key) }
        }
    }

    val allAttributesMap: Map<DeviceAttribute, ByteArray>
        get() = attributesMap.takeIf {
            DeviceAttribute.APP_DATA in it
        }?.let { map ->
            val appDataBytes = map.getValue(DeviceAttribute.APP_DATA)
            map.filterNot { it.key == DeviceAttribute.APP_DATA } + if (isAppDataValueMap(appDataBytes)) {
                decodeAppDataValueMap(appDataBytes)
            } else {
                mapOf(DeviceAttribute.APP_DATA to appDataBytes)
            }
        } ?: attributesMap

    override fun encodeContent(): ByteArray {
        val attributeShortMap = attributesMap.mapKeys { it.key.value }
        return ByteBuffer.allocate(
            Short.SIZE_BYTES + // deviceType
                    Byte.SIZE_BYTES + // flags
                    Byte.SIZE_BYTES + // deviceNumber
                    attributeShortMap.shortMapTotalBytes() // attributeMap
        )
            .putShort(deviceType.value)
            .put(flags)
            .put(deviceNumber)
            .putShortKeyBytesMap(attributeShortMap)
            .array()
    }

    enum class DeviceType(val value: Short) {
        IOT(1),
        MI_ROUTER(2),
        MI_SOUND_BOX(3),
        MI_LAPTOP(4),
        MI_TV(5),
        MI_PHONE(6),
        IOT_USER_ENV(7);

        companion object {
            private val valuesMap by lazy { entries.associateBy { it.value } }

            fun parse(value: Short) = valuesMap.getValue(value)
        }
    }

    enum class DeviceAttribute(val value: Short) {
        WIFI_MAC_ADDRESS(1),
        BLUETOOTH_MAC_ADDRESS(2),
        NIC_MAC_ADDRESS(3),
        IP_ADDRESS(4),
        PORT_1(5),
        PORT_2(6),
        PORT_3(7),
        ID_HASH(8),
        DEVICE_TOKEN(9),
        AUTH_TOKEN(10),
        DEVICE_NAME(11),
        DEVICE_TYPE(12),
        APP_DATA(13),
        USER_ENV_TOKEN(14),
        SSID(15),
        PASSWORD(17),
        MODEL(18);

        companion object {
            private val valuesMap by lazy { entries.associateBy { it.value } }

            fun parse(value: Short) = valuesMap.getValue(value)
        }
    }
}
