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
import lib.xfy9326.xiaomi.nfc.AppData
import lib.xfy9326.xiaomi.nfc.HandoffAppData
import lib.xfy9326.xiaomi.nfc.MiConnectData
import lib.xfy9326.xiaomi.nfc.NfcTagActionRecord
import lib.xfy9326.xiaomi.nfc.NfcTagAppData
import lib.xfy9326.xiaomi.nfc.NfcTagDeviceRecord
import lib.xfy9326.xiaomi.nfc.XiaomiNdefPayloadType
import lib.xfy9326.xiaomi.nfc.XiaomiNfcPayload
import lib.xfy9326.xiaomi.nfc.XiaomiNfcProtocol
import org.json.JSONObject
import tool.xfy9326.milink.nfc.utils.EMPTY
import tool.xfy9326.milink.nfc.utils.toHexString

object XiaomiNfc {
    private const val FLAG_BIND_TREAT_LIKE_VISIBLE_FOREGROUND_SERVICE = 268435456

    private const val PERMISSION_RECEIVE_ENDPOINT =
        "com.xiaomi.mi_connect_service.permission.RECEIVE_ENDPOINT"
    private const val URI_MI_HOME = "https://g.home.mi.com"

    private const val PKG_MI_CONNECT_SERVICE = "com.xiaomi.mi_connect_service"
    private const val PKG_SMART_HOME = "com.xiaomi.smarthome"

    fun getXiaomiNfcPayloadType(ndefMessage: NdefMessage): XiaomiNdefPayloadType? =
        ndefMessage.records.asSequence().filterNotNull().filter {
            it.tnf == NdefRecord.TNF_EXTERNAL_TYPE
        }.mapNotNull {
            runCatching {
                XiaomiNdefPayloadType.parse(it.type.toString(Charsets.US_ASCII))
            }.getOrNull()
        }.firstOrNull()

    fun getXiaomiNfcPayloadBytes(
        ndefMessage: NdefMessage,
        type: XiaomiNdefPayloadType
    ): ByteArray? =
        ndefMessage.records.asSequence().filterNotNull().filter {
            it.tnf == NdefRecord.TNF_EXTERNAL_TYPE && it.type.toString(Charsets.US_ASCII) == type.value
        }.firstOrNull()?.payload

    private fun newMiTapNdefMessage(ndefRecord: NdefRecord): NdefMessage =
        NdefMessage(
            ndefRecord,
            NdefRecord.createApplicationRecord(PKG_SMART_HOME),
            NdefRecord.createApplicationRecord(PKG_MI_CONNECT_SERVICE),
            NdefRecord.createUri(URI_MI_HOME.toUri())
        )

    abstract class NfcAction<T, A : AppData>(
        private val majorVersion: Int,
        private val minorVersion: Int,
        private val idHash: Byte? = null,
        private val protocol: XiaomiNfcProtocol<A>,
        protected val ndefRecordType: XiaomiNdefPayloadType
    ) {
        protected abstract fun encodeAppsData(config: T): A

        open fun newNdefMessage(config: T, shrink: Boolean = false): NdefMessage =
            NdefMessage(newNdefRecord(config))

        protected fun newNdefRecord(config: T): NdefRecord =
            NdefRecord(
                NdefRecord.TNF_EXTERNAL_TYPE,
                ndefRecordType.bytes,
                null,
                MiConnectData.from(build(config)).toByteArray()
            )

        fun newNdefDiscoveredIntent(tag: Tag?, id: ByteArray?, config: T): Intent {
            return Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                ndefRecordType.value.takeIf { it.isNotEmpty() }?.let {
                    data = "vnd.android.nfc://ext/${it}".toUri()
                }
                putExtra(NfcAdapter.EXTRA_TAG, tag)
                putExtra(NfcAdapter.EXTRA_ID, id)
                putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(newNdefMessage(config)))
            }
        }

        fun build(config: T): XiaomiNfcPayload<A> =
            XiaomiNfcPayload(
                majorVersion = majorVersion,
                minorVersion = minorVersion,
                idHash = idHash,
                protocol = protocol,
                appData = encodeAppsData(config)
            )
    }

    object EmptyMiTap : NfcAction<Int, NfcTagAppData>(
        majorVersion = 1,
        minorVersion = 2,
        idHash = 0,
        protocol = XiaomiNfcProtocol.V1,
        ndefRecordType = XiaomiNdefPayloadType.SMART_HOME
    ) {
        private const val MAJOR_VERSION = 1.toByte()
        private const val MINOR_VERSION = 0.toByte()
        private const val FLAGS = 0.toByte()
        private const val DEVICE_NUMBER = 0.toByte()

        override fun newNdefMessage(config: Int, shrink: Boolean): NdefMessage =
            if (shrink) {
                super.newNdefMessage(config, true)
            } else {
                newMiTapNdefMessage(newNdefRecord(config))
            }

        override fun encodeAppsData(config: Int): NfcTagAppData =
            NfcTagAppData(
                majorVersion = MAJOR_VERSION,
                minorVersion = MINOR_VERSION,
                writeTime = config,
                flags = FLAGS,
                records = listOf(
                    NfcTagDeviceRecord.newInstance(
                        deviceType = NfcTagDeviceRecord.DeviceType.IOT,
                        flags = FLAGS,
                        deviceNumber = DEVICE_NUMBER,
                        attributesMap = emptyMap()
                    ),
                    NfcTagActionRecord.newInstance(
                        action = NfcTagActionRecord.Action.EMPTY,
                        condition = NfcTagActionRecord.Condition.AUTO,
                        deviceNumber = DEVICE_NUMBER,
                        flags = FLAGS
                    )
                )
            )
    }

    object MiTapSoundBox : NfcAction<MiTapSoundBox.Config, NfcTagAppData>(
        majorVersion = 1,
        minorVersion = 2,
        idHash = 0,
        protocol = XiaomiNfcProtocol.V1,
        ndefRecordType = XiaomiNdefPayloadType.MI_CONNECT_SERVICE
    ) {
        private const val MAJOR_VERSION = 1.toByte()
        private const val MINOR_VERSION = 0.toByte()
        private const val FLAGS = 0.toByte()
        private const val DEVICE_NUMBER = 0.toByte()

        class Config(
            val writeTime: Int,
            val wifiMac: ByteArray?,
            val bluetoothMac: ByteArray,
            val model: String?
        )

        override fun newNdefMessage(config: Config, shrink: Boolean): NdefMessage =
            if (shrink) {
                super.newNdefMessage(config, true)
            } else {
                newMiTapNdefMessage(newNdefRecord(config))
            }

        override fun encodeAppsData(config: Config): NfcTagAppData =
            NfcTagAppData(
                majorVersion = MAJOR_VERSION,
                minorVersion = MINOR_VERSION,
                writeTime = config.writeTime,
                flags = FLAGS,
                records = listOf(
                    NfcTagDeviceRecord.newInstance(
                        deviceType = NfcTagDeviceRecord.DeviceType.MI_SOUND_BOX,
                        flags = FLAGS,
                        deviceNumber = DEVICE_NUMBER,
                        attributesMap = mutableMapOf(
                            NfcTagDeviceRecord.DeviceAttribute.BLUETOOTH_MAC_ADDRESS to config.bluetoothMac
                        ).apply {
                            config.wifiMac?.let {
                                this[NfcTagDeviceRecord.DeviceAttribute.WIFI_MAC_ADDRESS] = it
                            }
                            config.model?.let {
                                this[NfcTagDeviceRecord.DeviceAttribute.MODEL] =
                                    it.toByteArray(Charsets.UTF_8)
                            }
                        }
                    ),
                    NfcTagActionRecord.newInstance(
                        action = NfcTagActionRecord.Action.AUTO,
                        condition = NfcTagActionRecord.Condition.AUTO,
                        deviceNumber = DEVICE_NUMBER,
                        flags = FLAGS
                    )
                )
            )
    }

    object Circulate : NfcAction<Circulate.Config, NfcTagAppData>(
        majorVersion = 1,
        minorVersion = 11,
        idHash = 0,
        protocol = XiaomiNfcProtocol.V2,
        ndefRecordType = XiaomiNdefPayloadType.MI_CONNECT_SERVICE
    ) {
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
            val deviceType: NfcTagDeviceRecord.DeviceType,
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
                    NfcTagDeviceRecord.newInstance(
                        deviceType = config.deviceType,
                        flags = FLAGS,
                        deviceNumber = DEVICE_NUMBER,
                        attributesMap = mapOf(
                            NfcTagDeviceRecord.DeviceAttribute.WIFI_MAC_ADDRESS to config.wifiMac,
                            NfcTagDeviceRecord.DeviceAttribute.BLUETOOTH_MAC_ADDRESS to config.bluetoothMac
                        )
                    ),
                    NfcTagActionRecord.newInstance(
                        action = NfcTagActionRecord.Action.CUSTOM,
                        condition = NfcTagActionRecord.Condition.AUTO,
                        deviceNumber = DEVICE_NUMBER,
                        flags = FLAGS
                    )
                )
            )

        @SuppressLint("WrongConstant")
        fun sendBroadcast(context: Context, config: Config) {
            val appData = build(config).appData
            Intent(ACTION).apply {
                addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                addFlags(FLAG_BIND_TREAT_LIKE_VISIBLE_FOREGROUND_SERVICE)
                putExtra(EXTRA_ACTION, NfcTagActionRecord.Action.CUSTOM.value.toInt())
                appData.getFirstDeviceEnumAttributesMap(ndefRecordType).let { map ->
                    val idHash = map[NfcTagDeviceRecord.DeviceAttribute.ID_HASH]?.let {
                        Base64.encodeToString(
                            it,
                            Base64.DEFAULT
                        )
                    } ?: EMPTY
                    val wifiMac =
                        map[NfcTagDeviceRecord.DeviceAttribute.WIFI_MAC_ADDRESS]?.toHexString(true)
                            ?: EMPTY
                    val bluetoothMac =
                        map[NfcTagDeviceRecord.DeviceAttribute.BLUETOOTH_MAC_ADDRESS]?.toHexString(
                            true
                        ) ?: EMPTY
                    putExtra(EXTRA_ID_HASH, idHash)
                    putExtra(EXTRA_WIFI_MAC, wifiMac)
                    putExtra(EXTRA_BLUETOOTH_MAC, bluetoothMac)
                }
            }.also {
                context.sendBroadcast(it, PERMISSION_RECEIVE_ENDPOINT)
            }
        }
    }

    abstract class HandoffNfcAction<T : HandoffNfcAction.Config> : NfcAction<T, HandoffAppData>(
        majorVersion = 1,
        minorVersion = 13,
        idHash = null,
        protocol = XiaomiNfcProtocol.HandOff,
        ndefRecordType = XiaomiNdefPayloadType.MI_CONNECT_SERVICE
    ) {
        companion object {
            private const val MAJOR_VERSION = 0x27.toByte()
            private const val MINOR_VERSION = 0x17.toByte()

            private const val ACTION_PREFIX = "com.xiaomi.nfc.action."
            private const val EXTRA_PROTOCOL_VALUE_KEY = "protocol_value_key"
            private const val KEY_DEVICE_TYPE = "device_type_key"
            private const val KEY_PROTOCOL_PAYLOAD = "protocol_payload_key"

            private const val ACTION_TAG_DISCOVERED = "TAG_DISCOVERED"
        }

        interface Config {
            val deviceType: HandoffAppData.DeviceType
        }

        override fun encodeAppsData(config: T): HandoffAppData =
            HandoffAppData.newInstance(
                majorVersion = MAJOR_VERSION,
                minorVersion = MINOR_VERSION,
                deviceType = config.deviceType,
                attributesMap = emptyMap(),
                action = ACTION_TAG_DISCOVERED,
                payloadsMap = onBuildPayloadsMap(config)
            )

        protected abstract fun onBuildPayloadsMap(config: T): Map<HandoffAppData.PayloadKey, ByteArray>

        @SuppressLint("WrongConstant")
        fun sendBroadcast(context: Context, config: T) {
            val jsonObject = JSONObject().apply {
                put(KEY_DEVICE_TYPE, config.deviceType.value)
                put(KEY_PROTOCOL_PAYLOAD,
                    HandoffAppData.encodePayloadsMap(onBuildPayloadsMap(config)).let {
                        Base64.encodeToString(it, Base64.DEFAULT)
                    }
                )
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

    object ScreenMirror : HandoffNfcAction<ScreenMirror.Config>() {
        private const val ACTION_SUFFIX_MIRROR = "MIRROR"
        private const val FLAG_ABILITY_LYRA = 0x00000001.toByte()

        class Config(
            override val deviceType: HandoffAppData.DeviceType,
            val bluetoothMac: String,
            val enableLyra: Boolean
        ) : HandoffNfcAction.Config

        override fun onBuildPayloadsMap(config: Config): Map<HandoffAppData.PayloadKey, ByteArray> =
            mutableMapOf(
                HandoffAppData.PayloadKey.ACTION_SUFFIX to ACTION_SUFFIX_MIRROR.toByteArray(Charsets.UTF_8),
                HandoffAppData.PayloadKey.BLUETOOTH_MAC to config.bluetoothMac.toByteArray(Charsets.UTF_8)
            ).also {
                if (config.enableLyra) it[HandoffAppData.PayloadKey.EXT_ABILITY] =
                    byteArrayOf(FLAG_ABILITY_LYRA)
            }
    }

    @Suppress("unused")
    object TVCast : HandoffNfcAction<TVCast.Config>() {
        private const val ACTION_SUFFIX_TV_CAST = "TVCAST"

        class Config(
            override val deviceType: HandoffAppData.DeviceType,
            val wifiMac: String,
            val bluetoothMac: String
        ) : HandoffNfcAction.Config

        override fun onBuildPayloadsMap(config: Config): Map<HandoffAppData.PayloadKey, ByteArray> =
            mapOf(
                HandoffAppData.PayloadKey.ACTION_SUFFIX to ACTION_SUFFIX_TV_CAST.toByteArray(
                    Charsets.UTF_8
                ),
                HandoffAppData.PayloadKey.WIFI_MAC to config.wifiMac.toByteArray(Charsets.UTF_8),
                HandoffAppData.PayloadKey.BLUETOOTH_MAC to config.bluetoothMac.toByteArray(Charsets.UTF_8),
            )
    }
}