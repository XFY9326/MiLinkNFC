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
import lib.xfy9326.xiaomi.nfc.AppsData
import lib.xfy9326.xiaomi.nfc.HandoffAppData
import lib.xfy9326.xiaomi.nfc.NfcTagActionRecord
import lib.xfy9326.xiaomi.nfc.NfcTagAppData
import lib.xfy9326.xiaomi.nfc.NfcTagDeviceRecord
import lib.xfy9326.xiaomi.nfc.XiaomiNfcPayload
import lib.xfy9326.xiaomi.nfc.XiaomiNfcProtocol
import lib.xfy9326.xiaomi.nfc.decodeAsMiConnectPayload
import lib.xfy9326.xiaomi.nfc.toXiaomiNfcPayload
import org.json.JSONObject
import tool.xfy9326.milink.nfc.utils.EMPTY
import tool.xfy9326.milink.nfc.utils.toHexString

object XiaomiNfc {
    private const val FLAG_BIND_TREAT_LIKE_VISIBLE_FOREGROUND_SERVICE = 268435456

    private const val PERMISSION_RECEIVE_ENDPOINT = "com.xiaomi.mi_connect_service.permission.RECEIVE_ENDPOINT"

    @Suppress("SpellCheckingInspection")
    private const val NFC_EXTERNAL_TYPE = "com.xiaomi.mi_connect_service:externaltype"

    fun getXiaomiNfcPayloadBytes(ndefMessage: NdefMessage): ByteArray? =
        ndefMessage.records.asSequence().filterNotNull().filter {
            it.toMimeType() == NFC_EXTERNAL_TYPE
        }.firstOrNull()?.payload

    abstract class NfcAction<T, A : AppsData>(
        private val majorVersion: Int,
        private val minorVersion: Int,
        private val idHash: Byte? = null,
        private val protocol: XiaomiNfcProtocol<A>,
    ) {
        protected abstract fun encodeAppsData(config: T): A

        open fun newNdefMessage(config: T): NdefMessage =
            NdefMessage(newNdefRecord(config))

        protected fun newNdefRecord(config: T): NdefRecord =
            NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, NFC_EXTERNAL_TYPE.toByteArray(Charsets.US_ASCII), null, encode(config).encode())

        fun newNdefDiscoveredIntent(tag: Tag?, id: ByteArray?, config: T): Intent {
            return Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                data = "vnd.android.nfc://ext/$NFC_EXTERNAL_TYPE".toUri()
                putExtra(NfcAdapter.EXTRA_TAG, tag)
                putExtra(NfcAdapter.EXTRA_ID, id)
                putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(newNdefMessage(config)))
            }
        }

        fun encode(config: T): XiaomiNfcPayload<A> =
            XiaomiNfcPayload(
                majorVersion = majorVersion,
                minorVersion = minorVersion,
                idHash = idHash,
                protocol = protocol,
                appsData = encodeAppsData(config)
            )

        fun decode(bytes: ByteArray): XiaomiNfcPayload<A> {
            return bytes.decodeAsMiConnectPayload().toXiaomiNfcPayload(protocol)
        }
    }

    object SoundBoxPlay : NfcAction<SoundBoxPlay.Config, NfcTagAppData>(1, 2, 0, XiaomiNfcProtocol.V1) {
        private const val MAJOR_VERSION = 1.toByte()
        private const val MINOR_VERSION = 0.toByte()
        private const val FLAGS = 0.toByte()
        private const val DEVICE_NUMBER = 0.toByte()

        private const val URI_MI_HOME = "https://g.home.mi.com"

        class Config(
            val writeTime: Int,
            val wifiMac: ByteArray,
            val bluetoothMac: ByteArray,
            val model: String
        )

        private fun newMiHomeUriNdefRecord(): NdefRecord = NdefRecord.createUri(URI_MI_HOME.toUri())

        override fun newNdefMessage(config: Config): NdefMessage =
            NdefMessage(newNdefRecord(config), newMiHomeUriNdefRecord())

        override fun encodeAppsData(config: Config): NfcTagAppData =
            NfcTagAppData(
                majorVersion = MAJOR_VERSION,
                minorVersion = MINOR_VERSION,
                writeTime = config.writeTime,
                flags = FLAGS,
                records = listOf(
                    NfcTagDeviceRecord(
                        deviceType = NfcTagDeviceRecord.DeviceType.MI_SOUND_BOX,
                        flags = FLAGS,
                        deviceNumber = DEVICE_NUMBER,
                        attributesMap = mapOf(
                            NfcTagDeviceRecord.DeviceAttribute.WIFI_MAC_ADDRESS to config.wifiMac,
                            NfcTagDeviceRecord.DeviceAttribute.BLUETOOTH_MAC_ADDRESS to config.bluetoothMac,
                            NfcTagDeviceRecord.DeviceAttribute.MODEL to config.model.toByteArray(Charsets.UTF_8)
                        )
                    ),
                    NfcTagActionRecord(
                        action = NfcTagActionRecord.Action.AUTO,
                        condition = NfcTagActionRecord.Condition.AUTO,
                        deviceNumber = DEVICE_NUMBER,
                        flags = FLAGS,
                        conditionParameters = ByteArray(0)
                    )
                )
            )
    }

    object SoundBoxCirculate : NfcAction<SoundBoxCirculate.Config, NfcTagAppData>(1, 11, 0, XiaomiNfcProtocol.V2) {
        private const val MAJOR_VERSION = 1.toByte()
        private const val MINOR_VERSION = 0.toByte()
        private const val FLAGS = 0.toByte()
        private const val DEVICE_NUMBER = 0.toByte()

        private const val ACTION = "com.xiaomi.nfc.action.TAG_DISCOVERED"
        private const val EXTRA_ACTION = "Action"
        private const val EXTRA_ID_HASH = "IdHash"
        private const val EXTRA_WIFI_MAC = "WifiMac"
        private const val EXTRA_BLUETOOTH_MAC = "BtAddress"

        class Config(
            val writeTime: Int,
            val wifiMac: ByteArray,
            val bluetoothMac: ByteArray
        )

        override fun encodeAppsData(config: Config): NfcTagAppData =
            NfcTagAppData(
                majorVersion = MAJOR_VERSION,
                minorVersion = MINOR_VERSION,
                writeTime = config.writeTime,
                flags = FLAGS,
                records = listOf(
                    NfcTagDeviceRecord(
                        deviceType = NfcTagDeviceRecord.DeviceType.MI_SOUND_BOX,
                        flags = FLAGS,
                        deviceNumber = DEVICE_NUMBER,
                        attributesMap = mapOf(
                            NfcTagDeviceRecord.DeviceAttribute.WIFI_MAC_ADDRESS to config.wifiMac,
                            NfcTagDeviceRecord.DeviceAttribute.BLUETOOTH_MAC_ADDRESS to config.bluetoothMac
                        )
                    ),
                    NfcTagActionRecord(
                        action = NfcTagActionRecord.Action.CUSTOM,
                        condition = NfcTagActionRecord.Condition.AUTO,
                        deviceNumber = DEVICE_NUMBER,
                        flags = FLAGS,
                        conditionParameters = ByteArray(0)
                    )
                )
            )

        @SuppressLint("WrongConstant")
        fun sendBroadcast(context: Context, appData: NfcTagAppData) {
            Intent(ACTION).apply {
                addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                addFlags(FLAG_BIND_TREAT_LIKE_VISIBLE_FOREGROUND_SERVICE)
                putExtra(EXTRA_ACTION, NfcTagActionRecord.Action.CUSTOM.value.toInt())
                appData.getDeviceRecords().firstOrNull()?.attributesMap?.let { map ->
                    val idHash = map[NfcTagDeviceRecord.DeviceAttribute.ID_HASH]?.let { Base64.encodeToString(it, Base64.DEFAULT) } ?: EMPTY
                    val wifiMac = map[NfcTagDeviceRecord.DeviceAttribute.WIFI_MAC_ADDRESS]?.toHexString(true) ?: EMPTY
                    val btMac = map[NfcTagDeviceRecord.DeviceAttribute.BLUETOOTH_MAC_ADDRESS]?.toHexString(true) ?: EMPTY
                    putExtra(EXTRA_ID_HASH, idHash)
                    putExtra(EXTRA_WIFI_MAC, wifiMac)
                    putExtra(EXTRA_BLUETOOTH_MAC, btMac)
                }
            }.also {
                context.sendBroadcast(it, PERMISSION_RECEIVE_ENDPOINT)
            }
        }
    }

    object ScreenMirror : NfcAction<ScreenMirror.Config, HandoffAppData>(1, 13, null, XiaomiNfcProtocol.HandOff) {
        private const val MAJOR_VERSION = 0x27.toByte()
        private const val MINOR_VERSION = 0x17.toByte()

        private const val ACTION_PREFIX = "com.xiaomi.nfc.action."
        private const val EXTRA_PROTOCOL_VALUE_KEY = "protocol_value_key"
        private const val KEY_DEVICE_TYPE = "device_type_key"
        private const val KEY_PROTOCOL_PAYLOAD = "protocol_payload_key"

        private const val ACTION_TAG_DISCOVERED = "TAG_DISCOVERED"
        private const val ACTION_SUFFIX_MIRROR = "MIRROR"

        private const val FLAG_ABILITY_LYRA = 0x00000001.toByte()

        class Config(
            val deviceType: HandoffAppData.DeviceType,
            val bluetoothMac: String,
            val enableLyra: Boolean
        )

        override fun encodeAppsData(config: Config): HandoffAppData =
            HandoffAppData(
                majorVersion = MAJOR_VERSION,
                minorVersion = MINOR_VERSION,
                deviceType = config.deviceType,
                attributesMap = emptyMap(),
                action = ACTION_TAG_DISCOVERED,
                payloadsMap = config.buildPayloadsMap()
            )

        private fun Config.buildPayloadsMap(): Map<HandoffAppData.PayloadKey, ByteArray> =
            mutableMapOf(
                HandoffAppData.PayloadKey.ACTION_SUFFIX to ACTION_SUFFIX_MIRROR.toByteArray(Charsets.UTF_8),
                HandoffAppData.PayloadKey.BT_MAC to bluetoothMac.toByteArray(Charsets.UTF_8)
            ).also {
                if (enableLyra) it[HandoffAppData.PayloadKey.EXT_ABILITY] = byteArrayOf(FLAG_ABILITY_LYRA)
            }

        @SuppressLint("WrongConstant")
        fun sendBroadcast(context: Context, config: Config) {
            val jsonObject = JSONObject().apply {
                put(KEY_DEVICE_TYPE, config.deviceType.value)
                put(KEY_PROTOCOL_PAYLOAD, HandoffAppData.encodePayloadsMap(config.buildPayloadsMap()))
            }

            Intent(ACTION_PREFIX + ACTION_TAG_DISCOVERED).apply {
                addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                addFlags(FLAG_BIND_TREAT_LIKE_VISIBLE_FOREGROUND_SERVICE)
                putExtra(EXTRA_PROTOCOL_VALUE_KEY, jsonObject.toString())
            }.also {
                context.sendBroadcast(it, PERMISSION_RECEIVE_ENDPOINT)
            }
        }
    }
}