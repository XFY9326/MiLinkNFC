package tool.xfy9326.milink.nfc.protocol

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.util.Base64
import androidx.core.net.toUri
import com.google.protobuf.ByteString
import org.json.JSONObject
import tool.xfy9326.milink.nfc.proto.MiConnectProto
import tool.xfy9326.milink.nfc.utils.EMPTY
import tool.xfy9326.milink.nfc.utils.toHexString
import java.nio.ByteBuffer

@Suppress("unused")
object XiaomiNfc {
    @Suppress("SpellCheckingInspection")
    private const val MI_CONNECT_NAME = "MI-NFCTAG"
    private const val MI_CONNECT_DEVICE_TYPE_NFC = 15
    private const val MI_CONNECT_APP_ID_NFC = 16378

    private const val URI_MI_HOME = "https://g.home.mi.com"

    private const val FLAG_BIND_TREAT_LIKE_VISIBLE_FOREGROUND_SERVICE = 268435456

    private const val PERMISSION_RECEIVE_ENDPOINT = "com.xiaomi.mi_connect_service.permission.RECEIVE_ENDPOINT"

    @Suppress("SpellCheckingInspection")
    private const val NFC_EXTERNAL_TYPE = "com.xiaomi.mi_connect_service:externaltype"
    private const val NFC_EXTERNAL_URI = "vnd.android.nfc://ext/$NFC_EXTERNAL_TYPE"

    fun createNdefMessage(payload: MiConnectProto.AttrOps, addMiHomeUri: Boolean = false): NdefMessage {
        val record = NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, NFC_EXTERNAL_TYPE.toByteArray(Charsets.US_ASCII), null, payload.toByteArray())
        return if (addMiHomeUri) {
            NdefMessage(record, NdefRecord.createUri(URI_MI_HOME.toUri()))
        } else {
            NdefMessage(record)
        }
    }

    fun newNdefActivityIntent(tag: Tag?, id: ByteArray?, ndefMessage: NdefMessage): Intent {
        return Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            data = NFC_EXTERNAL_URI.toUri()
            putExtra(NfcAdapter.EXTRA_TAG, tag)
            putExtra(NfcAdapter.EXTRA_ID, id)
            putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(ndefMessage))
        }
    }

    private fun buildMiConnectData(version: Pair<Int, Int>, flags: Byte, idHash: Byte?, appData: ByteArray): MiConnectProto.AttrOps {
        val advData = MiConnectProto.AttrAdvData.newBuilder()
            .setVersionMajor(version.first)
            .setVersionMinor(version.second)
            .setFlags(ByteString.copyFrom(byteArrayOf(flags)))
            .setName(MI_CONNECT_NAME)
            .setDeviceType(MI_CONNECT_DEVICE_TYPE_NFC)
            .apply {
                idHash?.let { setIdHash(ByteString.copyFrom(byteArrayOf(it))) }
            }
            .addAppIds(MI_CONNECT_APP_ID_NFC)
            .addAppsData(ByteString.copyFrom(appData))
            .build()
        return MiConnectProto.AttrOps.newBuilder()
            .setAdvData(advData)
            .build()
    }

    abstract class NfcTagRecord(private val type: Byte) {
        protected abstract fun encodeContent(): ByteArray

        fun toByteArray(): ByteArray {
            val content = encodeContent()
            val totalSize = Byte.SIZE_BYTES + Short.SIZE_BYTES + content.size
            return ByteBuffer.allocate(totalSize)
                .put(type)
                .putShort(totalSize.toShort())
                .put(content)
                .array()
        }
    }

    data class NfcTagAppData(
        val version: Pair<Byte, Byte>,
        val writeTime: Int,
        val flags: Byte,
        val records: List<NfcTagRecord>
    ) {
        fun getDeviceRecords(): List<NfcTagDeviceRecord> =
            records.filterIsInstance<NfcTagDeviceRecord>()

        fun getActionRecords(): List<NfcTagActionRecord> =
            records.filterIsInstance<NfcTagActionRecord>()

        fun toByteArray(): ByteArray {
            val recordByteArrays = records.map { it.toByteArray() }
            return ByteBuffer.allocate(
                Byte.SIZE_BYTES * 2 + // version
                        Int.SIZE_BYTES + // writeTime
                        Byte.SIZE_BYTES + // flags
                        recordByteArrays.totalBytes() // records
            )
                .put(version.first)
                .put(version.second)
                .putInt(writeTime)
                .put(flags)
                .apply {
                    for (bytes in recordByteArrays) put(bytes)
                }
                .array()
        }
    }

    data class NfcTagDeviceRecord(
        val deviceType: DeviceType,
        val flags: Byte,
        val deviceNumber: Byte,
        val attributeMap: Map<DeviceAttribute, ByteArray>
    ) : NfcTagRecord(1) {

        companion object {
            private const val PREFIX_ATTR_APP_DATA = "mxD"

            fun buildAttrAppData(map: Map<DeviceAttribute, ByteArray>): ByteArray {
                val prefix = PREFIX_ATTR_APP_DATA.toByteArray(Charsets.UTF_8)
                val attributeShortMap = map.mapKeys { it.key.value }
                return ByteBuffer.allocate(prefix.size + attributeShortMap.shortMapTotalBytes())
                    .put(prefix)
                    .putShortKeyBytesMap(attributeShortMap)
                    .array()
            }
        }

        override fun encodeContent(): ByteArray {
            val attributeShortMap = attributeMap.mapKeys { it.key.value }
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
            IOT_USER_ENV(7)
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
            MODEL(18)
        }
    }

    data class NfcTagActionRecord(
        val action: Action,
        val condition: Byte,
        val deviceNumber: Byte,
        val flags: Byte,
        val conditionParameters: ByteArray = ByteArray(0)
    ) : NfcTagRecord(2) {
        override fun encodeContent(): ByteArray {
            return ByteBuffer.allocate(
                Short.SIZE_BYTES + // action
                        Byte.SIZE_BYTES + // condition
                        Byte.SIZE_BYTES + // deviceNumber
                        Byte.SIZE_BYTES + // flags
                        conditionParameters.size // conditionParameters
            )
                .putShort(action.value)
                .put(condition)
                .put(deviceNumber)
                .put(flags)
                .put(conditionParameters)
                .array()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NfcTagActionRecord

            if (action != other.action) return false
            if (condition != other.condition) return false
            if (deviceNumber != other.deviceNumber) return false
            if (flags != other.flags) return false
            return conditionParameters.contentEquals(other.conditionParameters)
        }

        override fun hashCode(): Int {
            var result = action.hashCode()
            result = 31 * result + condition
            result = 31 * result + deviceNumber
            result = 31 * result + flags
            result = 31 * result + conditionParameters.contentHashCode()
            return result
        }

        enum class Action(val value: Short) {
            IOT(1),
            MUSIC_RELAY(2),
            TEL_RELAY(3),
            FILE_TRANSFER(4),
            SCREEN_CASTING(5),
            CORP_OPERATION(6),
            VIDEO_RELAY(7),
            VOIP_RELAY(8),
            IOT_ENV(9),
            REMOTE_CONTROLLER(10),
            GUEST_NETWORK(11),
            EMPTY(12),
            CUSTOM(13),
            AUTO(Short.MAX_VALUE)
        }

        enum class Condition(val value: Byte) {
            APP_FOREGROUND(1),
            SCREEN_LOCKED(2),
            AUTO(Byte.MAX_VALUE)
        }
    }

    object AIOTProtocol {
        private const val ADV_DATA_MAJOR_VERSION = 1
        private const val ADV_DATA_MINOR_VERSION = 2
        private const val ADV_DATA_FLAGS = 0.toByte()
        private const val ADV_DATA_ID_HASH = 0.toByte()

        fun buildNdefRecordPayload(appData: NfcTagAppData): MiConnectProto.AttrOps =
            buildMiConnectData(
                version = ADV_DATA_MAJOR_VERSION to ADV_DATA_MINOR_VERSION,
                flags = ADV_DATA_FLAGS,
                idHash = ADV_DATA_ID_HASH,
                appData = appData.toByteArray()
            )
    }

    object V2Protocol {
        private const val ADV_DATA_MAJOR_VERSION = 1
        private const val ADV_DATA_MINOR_VERSION = 11
        private const val ADV_DATA_FLAGS = 1.toByte()
        private const val ADV_DATA_ID_HASH = 0.toByte()

        private const val ACTION = "com.xiaomi.nfc.action.TAG_DISCOVERED"
        private const val EXTRA_ACTION = "Action"
        private const val EXTRA_ID_HASH = "IdHash"
        private const val EXTRA_WIFI_MAC = "WifiMac"
        private const val EXTRA_BT_MAC = "BtAddress"

        fun buildNdefRecordPayload(appData: NfcTagAppData): MiConnectProto.AttrOps =
            buildMiConnectData(
                version = ADV_DATA_MAJOR_VERSION to ADV_DATA_MINOR_VERSION,
                flags = ADV_DATA_FLAGS,
                idHash = ADV_DATA_ID_HASH,
                appData = appData.toByteArray()
            )

        @SuppressLint("WrongConstant")
        fun sendBroadcast(context: Context, appData: NfcTagAppData) {
            Intent(ACTION).apply {
                addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                addFlags(FLAG_BIND_TREAT_LIKE_VISIBLE_FOREGROUND_SERVICE)
                putExtra(EXTRA_ACTION, NfcTagActionRecord.Action.CUSTOM.value.toInt())
                appData.getDeviceRecords().firstOrNull()?.attributeMap?.let { map ->
                    val idHash = map[NfcTagDeviceRecord.DeviceAttribute.ID_HASH]?.let { Base64.encodeToString(it, Base64.DEFAULT) } ?: EMPTY
                    val wifiMac = map[NfcTagDeviceRecord.DeviceAttribute.WIFI_MAC_ADDRESS]?.toHexString(true) ?: EMPTY
                    val btMac = map[NfcTagDeviceRecord.DeviceAttribute.BLUETOOTH_MAC_ADDRESS]?.toHexString(true) ?: EMPTY
                    putExtra(EXTRA_ID_HASH, idHash)
                    putExtra(EXTRA_WIFI_MAC, wifiMac)
                    putExtra(EXTRA_BT_MAC, btMac)
                }
            }.also {
                context.sendBroadcast(it, PERMISSION_RECEIVE_ENDPOINT)
            }
        }
    }

    object NewProtocol {
        private const val ADV_DATA_MAJOR_VERSION = 1
        private const val ADV_DATA_MINOR_VERSION = 13
        private const val ADV_DATA_FLAGS = 3.toByte()

        private const val MAJOR_VERSION = 39.toByte()
        private const val MINOR_VERSION = 23.toByte()

        private const val ACTION_PREFIX = "com.xiaomi.nfc.action."
        private const val EXTRA_PROTOCOL_VALUE_KEY = "protocol_value_key"
        private const val KEY_DEVICE_TYPE = "device_type_key"
        private const val KEY_ATTRIBUTE_VALUE = "attribute_value_key"
        private const val KEY_PROTOCOL_PAYLOAD = "protocol_payload_key"

        data class AppData(
            val deviceType: DeviceType,
            val attributeMap: Map<Byte, ByteArray> = emptyMap(),
            val action: String,
            val payloadMap: Map<AttributeType, ByteArray>
        ) {
            fun toByteArray(): ByteArray {
                val actionBytes = action.toByteArray(Charsets.UTF_8)
                val payloadByteMap = payloadMap.mapKeys { it.key.value }
                return ByteBuffer.allocate(
                    Byte.SIZE_BYTES + // major version
                            Byte.SIZE_BYTES + // minor version
                            Int.SIZE_BYTES + // deviceType
                            Byte.SIZE_BYTES + // attributeMap size
                            attributeMap.bytesMapTotalBytes() + // attributeMap
                            Byte.SIZE_BYTES + // action size
                            actionBytes.size + // action
                            payloadByteMap.bytesMapTotalBytes() // payloadData
                )
                    .put(MAJOR_VERSION)
                    .put(MINOR_VERSION)
                    .putInt(deviceType.value)
                    .put(attributeMap.size.toByte())
                    .putByteKeyBytesMap(attributeMap)
                    .put(action.length.toByte())
                    .put(action.toByteArray(Charsets.UTF_8))
                    .putByteKeyBytesMap(payloadByteMap)
                    .array()
            }
        }

        fun buildNdefRecordPayload(appData: AppData): MiConnectProto.AttrOps =
            buildMiConnectData(
                version = ADV_DATA_MAJOR_VERSION to ADV_DATA_MINOR_VERSION,
                flags = ADV_DATA_FLAGS,
                idHash = null,
                appData = appData.toByteArray()
            )

        @SuppressLint("WrongConstant")
        fun sendBroadcast(context: Context, appData: AppData) {
            val jsonObject = JSONObject().apply {
                put(KEY_DEVICE_TYPE, appData.deviceType.value)
                if (appData.attributeMap.isNotEmpty()) put(KEY_ATTRIBUTE_VALUE, appData.attributeMap.encodeAttributeMap())
                if (appData.payloadMap.isNotEmpty()) put(KEY_PROTOCOL_PAYLOAD, appData.payloadMap.encodePayloadMap())
            }

            Intent(ACTION_PREFIX + appData.action).apply {
                addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                addFlags(FLAG_BIND_TREAT_LIKE_VISIBLE_FOREGROUND_SERVICE)
                putExtra(EXTRA_PROTOCOL_VALUE_KEY, jsonObject.toString())
            }.also {
                context.sendBroadcast(it, PERMISSION_RECEIVE_ENDPOINT)
            }
        }

        private fun Map<Byte, ByteArray>.encodeAttributeMap(): String =
            JSONObject().apply {
                for ((key, value) in this@encodeAttributeMap) {
                    // 这个写法参照源代码，如果使用了肯定会报错，目前用不到
                    put(key.toString(), value)
                }
            }.toString().toByteArray(Charsets.UTF_8).let {
                Base64.encodeToString(it, Base64.DEFAULT)
            }

        private fun Map<AttributeType, ByteArray>.encodePayloadMap(): String =
            mapKeys { it.key.value }.let {
                ByteBuffer.allocate(it.bytesMapTotalBytes()).putByteKeyBytesMap(it).array()
            }.let {
                Base64.encodeToString(it, Base64.DEFAULT)
            }

        enum class DeviceType(val value: Int) {
            TV(2),
            PC(3),
            CAR(5),
            PAD(8)
        }

        enum class AttributeType(val value: Byte) {
            ACTION_SUFFIX(101),
            BT_MAC(1),
            WIFI_MAC(2),
            WIRED_MAC(3),
            EXT_ABILITY(121)
        }
    }

    private fun List<ByteArray>.totalBytes(): Int =
        fold(0) { acc, bytes -> acc + bytes.size }

    private fun Map<Byte, ByteArray>.bytesMapTotalBytes(): Int =
        entries.fold(0) { acc, entry -> acc + Byte.SIZE_BYTES * 2 + entry.value.size }

    private fun Map<Short, ByteArray>.shortMapTotalBytes(): Int =
        entries.fold(0) { acc, entry -> acc + Short.SIZE_BYTES * 2 + entry.value.size }

    private fun ByteBuffer.putByteKeyBytesMap(map: Map<Byte, ByteArray>): ByteBuffer {
        for ((key, value) in map) {
            put(key)
            put(value.size.toByte())
            put(value)
        }
        return this
    }

    private fun ByteBuffer.putShortKeyBytesMap(map: Map<Short, ByteArray>): ByteBuffer {
        for ((key, value) in map) {
            putShort(key)
            putShort(value.size.toShort())
            put(value)
        }
        return this
    }
}