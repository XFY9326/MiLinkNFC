package lib.xfy9326.xiaomi.nfc

import java.nio.ByteBuffer

@Suppress("unused", "MemberVisibilityCanBePrivate")
data class NfcTagDeviceRecord(
    val deviceType: Short,
    val flags: Byte,
    val deviceNumber: Byte,
    val attributesMap: Map<Short, ByteArray>,
) : NfcTagRecord(TYPE_DEVICE) {

    companion object {
        private val PREFIX_APP_DATA_MAP by lazy { "mxD".toByteArray(Charsets.UTF_8) }

        fun newInstance(
            deviceType: DeviceType,
            flags: Byte,
            deviceNumber: Byte,
            attributesMap: Map<DeviceAttribute, ByteArray>,
        ) = NfcTagDeviceRecord(
            deviceType = deviceType.value,
            flags = flags,
            deviceNumber = deviceNumber,
            attributesMap = attributesMap.mapKeys { it.key.value }
        )

        fun getAppDataValueType(
            bytes: ByteArray,
            action: NfcTagActionRecord.Action,
            ndefType: XiaomiNdefPayloadType
        ): AppDataValueType {
            return if (
                ndefType == XiaomiNdefPayloadType.SMART_HOME &&
                (action == NfcTagActionRecord.Action.IOT || action == NfcTagActionRecord.Action.IOT_ENV)
            ) {
                AppDataValueType.IOT_ACTION
            } else if (ndefType == XiaomiNdefPayloadType.MI_CONNECT_SERVICE && bytes.startsWith(
                    PREFIX_APP_DATA_MAP
                )
            ) {
                AppDataValueType.ATTRIBUTES_MAP
            } else {
                AppDataValueType.UNKNOWN
            }
        }

        fun encodeAppDataValueMap(map: Map<DeviceAttribute, ByteArray>): ByteArray {
            val attributeShortMap = map.mapKeys { it.key.value }
            return ByteBuffer.allocate(PREFIX_APP_DATA_MAP.size + attributeShortMap.shortMapTotalBytes())
                .put(PREFIX_APP_DATA_MAP)
                .putShortKeyBytesMap(attributeShortMap)
                .array()
        }

        fun decodeAppDataValueMap(bytes: ByteArray): Map<DeviceAttribute, ByteArray> {
            val buffer = ByteBuffer.wrap(bytes)
            require(bytes.startsWith(PREFIX_APP_DATA_MAP)) { "Not an valid DeviceAttribute.APP_DATA map byte array" }
            return buffer.getShortKeyBytesMap().mapKeys { DeviceAttribute.parse(it.key) }
        }

        private fun Map<DeviceAttribute, ByteArray>.tryExtractAppDataValueMap(
            action: NfcTagActionRecord.Action,
            ndefType: XiaomiNdefPayloadType
        ) =
            takeIf {
                DeviceAttribute.APP_DATA in it
            }?.let { map ->
                val appDataBytes = map.getValue(DeviceAttribute.APP_DATA)
                val extMap = when (getAppDataValueType(appDataBytes, action, ndefType)) {
                    AppDataValueType.ATTRIBUTES_MAP -> decodeAppDataValueMap(appDataBytes)
                    else -> mapOf(DeviceAttribute.APP_DATA to appDataBytes)
                }
                map.filterNot { it.key == DeviceAttribute.APP_DATA } + extMap
            } ?: this
    }

    val enumDeviceType by lazy { DeviceType.parse(deviceType) }

    fun getAllAttributesMap(
        action: NfcTagActionRecord.Action,
        ndefType: XiaomiNdefPayloadType
    ): Map<DeviceAttribute, ByteArray> =
        when (ndefType) {
            XiaomiNdefPayloadType.SMART_HOME -> attributesMap.mapKeys {
                when (action) {
                    NfcTagActionRecord.Action.IOT -> DeviceAttribute.parseIOT(it.key)
                    NfcTagActionRecord.Action.IOT_ENV -> DeviceAttribute.parseIOTEnv(it.key)
                    else -> DeviceAttribute.parse(it.key)
                }
            }

            XiaomiNdefPayloadType.MI_CONNECT_SERVICE -> attributesMap.mapKeys {
                DeviceAttribute.parse(
                    it.key
                )
            }

            else -> emptyMap()
        }.tryExtractAppDataValueMap(action, ndefType)

    override fun encodeContent(): ByteArray {
        return ByteBuffer.allocate(
            Short.SIZE_BYTES + // deviceType
                    Byte.SIZE_BYTES + // flags
                    Byte.SIZE_BYTES + // deviceNumber
                    attributesMap.shortMapTotalBytes() // attributeMap
        )
            .putShort(deviceType)
            .put(flags)
            .put(deviceNumber)
            .putShortKeyBytesMap(attributesMap)
            .array()
    }

    enum class DeviceType(val value: Short) {
        UNKNOWN(0),
        IOT(1),
        MI_ROUTER(2),
        MI_SOUND_BOX(3),
        MI_LAPTOP(4),
        MI_TV(5),
        MI_PHONE(6),
        IOT_USER_ENV(7);

        companion object {
            private val valuesMap by lazy { entries.associateBy { it.value } }

            fun parse(value: Short) = valuesMap[value] ?: UNKNOWN
        }
    }

    enum class DeviceAttribute(val value: Short, val isText: Boolean? = null) {
        UNKNOWN(0),
        WIFI_MAC_ADDRESS(1, false),
        BLUETOOTH_MAC_ADDRESS(2, false),
        NIC_MAC_ADDRESS(3, false),
        IP_ADDRESS(4),
        PORT_1(5),
        PORT_2(6),
        PORT_3(7),
        ID_HASH(8, false),
        DEVICE_TOKEN(9),
        AUTH_TOKEN(10),
        DEVICE_NAME(11),
        DEVICE_TYPE(12),
        APP_DATA(13, false),
        USER_ENV_TOKEN(14),
        SSID(15, true),
        PASSWORD(17, true),
        MODEL(18, true),

        // IOT
        IOT_DEVICE_ID(6, true),
        IOT_USER_MODEL(7, true),
        IOT_NFC_EXTRA_DATA(8, true),
        IOT_DEVICE_MAC(2, true),
        IOT_APP_DATA(13, true),

        // IOT_ENV
        IOT_ENV_USER_ID(1, true),
        IOT_ENV_OWNER_UID(2, true),
        IOT_ENV_REGION(3, true),
        IOT_ENV_SCENE_NAME(4, true);

        val isIOT by lazy { name.startsWith(PREFIX_IOT) && !name.startsWith(PREFIX_IOT_ENV) }

        val isIOTEnv by lazy { name.startsWith(PREFIX_IOT_ENV) }

        val attributeName by lazy {
            if (isIOT) {
                name.substringAfter(PREFIX_IOT)
            } else if (isIOTEnv) {
                name.substringAfter(PREFIX_IOT_ENV)
            } else {
                name
            }
        }

        companion object {
            private const val PREFIX_IOT = "IOT_"
            private const val PREFIX_IOT_ENV = "IOT_ENV_"

            private val valuesMap by lazy {
                entries.filterNot { it.isIOT || it.isIOTEnv }.associateBy { it.value }
            }
            private val iotValuesMap by lazy {
                entries.filter { it.isIOT }.associateBy { it.value }
            }
            private val iotEnvValuesMap by lazy {
                entries.filter { it.isIOTEnv }.associateBy { it.value }
            }


            fun parse(value: Short) = valuesMap[value] ?: UNKNOWN

            fun parseIOT(value: Short) = iotValuesMap[value] ?: UNKNOWN

            fun parseIOTEnv(value: Short) = iotEnvValuesMap[value] ?: UNKNOWN
        }
    }

    enum class AppDataValueType {
        UNKNOWN,
        ATTRIBUTES_MAP,
        IOT_ACTION;
    }
}
