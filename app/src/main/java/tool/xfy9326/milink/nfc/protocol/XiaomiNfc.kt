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
import tool.xfy9326.milink.nfc.data.PackageData
import tool.xfy9326.milink.nfc.proto.MiConnectProto
import tool.xfy9326.milink.nfc.utils.getPackageData
import tool.xfy9326.milink.nfc.utils.getPackageMetaData
import java.io.ByteArrayOutputStream

object XiaomiNfc {
    private const val MI_CONNECT_MAJOR_VERSION = 1
    private const val MI_CONNECT_MINOR_VERSION = 13

    private const val APPS_DATA_MAJOR_VERSION = 39
    private const val APPS_DATA_MINOR_VERSION = 23

    @Suppress("SpellCheckingInspection")
    private const val MI_CONNECT_NAME = "MI-NFCTAG"
    private const val MI_CONNECT_DEVICE_TYPE = 15
    private const val MI_CONNECT_FLAG = 3.toByte()
    private const val MI_CONNECT_APP_ID = 16378

    const val NFC_NOTIFICATION_CHANNEL_ID = "tag_dispatch"

    private const val ACTION_SUFFIX_TAG_DISCOVERED = "TAG_DISCOVERED"
    private const val ACTION_SUFFIX_MIRROR = "MIRROR"

    private const val ACTION_NFC_TAG_DISCOVERED = "com.xiaomi.nfc.action.TAG_DISCOVERED"
    private const val FLAG_BIND_TREAT_LIKE_VISIBLE_FOREGROUND_SERVICE = 268435456
    private const val PERMISSION_RECEIVE_ENDPOINT = "com.xiaomi.mi_connect_service.permission.RECEIVE_ENDPOINT"

    private const val EXTRA_PROTOCOL_VALUE_KEY = "protocol_value_key"
    private const val KEY_DEVICE_TYPE = "device_type_key"
    private const val KEY_PROTOCOL_PAYLOAD = "protocol_payload_key"

    private const val TAG_DISCOVERED_KEY_ACTION_SUFFIX = 101
    private const val TAG_DISCOVERED_KEY_BT_MAC = 1
    private const val TAG_DISCOVERED_KEY_EXT_ABILITY = 121

    private const val FLAG_ABILITY_LYRA = 0x00000001

    @Suppress("SpellCheckingInspection")
    private const val NFC_EXTERNAL_TYPE = "com.xiaomi.mi_connect_service:externaltype"
    private val nfcExternalUri by lazy { "vnd.android.nfc://ext/$NFC_EXTERNAL_TYPE".toUri() }

    private const val PKG_MI_LINK_SERVICE = "com.milink.service"
    private const val PKG_MI_CONNECT_SERVICE = "com.xiaomi.mi_connect_service"

    private const val PKG_META_DATA_XIAOMI_CONTINUITY_VERSION_NAME = "com.xiaomi.continuity.VERSION_NAME"

    private val ONE_HOP_RELATED_PACKAGE_NAMES = arrayOf(
        PKG_MI_LINK_SERVICE,
        PKG_MI_CONNECT_SERVICE,
    )

    fun createNdefMsg(nfcDeviceType: NfcDeviceType, btMac: String, enableLyra: Boolean): NdefMessage {
        val payload = buildNfcPayload(nfcDeviceType, btMac, enableLyra)
        val record = NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, NFC_EXTERNAL_TYPE.toByteArray(Charsets.US_ASCII), null, payload)
        return NdefMessage(record)
    }

    fun newNdefActivityIntent(tag: Tag?, id: ByteArray?, nfcDeviceType: NfcDeviceType, btMac: String, enableLyra: Boolean): Intent {
        val messages = arrayOf(createNdefMsg(nfcDeviceType, btMac, enableLyra))
        return Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            data = nfcExternalUri
            putExtra(NfcAdapter.EXTRA_TAG, tag)
            putExtra(NfcAdapter.EXTRA_ID, id)
            putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, messages)
        }
    }

    @SuppressLint("WrongConstant")
    fun sendConnectServiceBroadcast(context: Context, nfcDeviceType: NfcDeviceType, btMac: String, enableLyra: Boolean) {
        val payload = buildNfcMirrorPayload(btMac, enableLyra)
        val jsonObject = JSONObject().apply {
            put(KEY_DEVICE_TYPE, nfcDeviceType.value)
            put(KEY_PROTOCOL_PAYLOAD, Base64.encodeToString(payload, Base64.DEFAULT))
        }

        Intent(ACTION_NFC_TAG_DISCOVERED).apply {
            addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            addFlags(FLAG_BIND_TREAT_LIKE_VISIBLE_FOREGROUND_SERVICE)
            putExtra(EXTRA_PROTOCOL_VALUE_KEY, jsonObject.toString())
        }.also {
            context.sendBroadcast(it, PERMISSION_RECEIVE_ENDPOINT)
        }
    }

    fun getRelatedPackageDataMap(context: Context): Map<String, PackageData?> =
        ONE_HOP_RELATED_PACKAGE_NAMES.associateWith { pkgName ->
            context.getPackageData(pkgName)
        }

    fun isLocalDeviceSupportLyra(context: Context): Boolean {
        return context.getPackageMetaData(PKG_MI_CONNECT_SERVICE)?.getString(PKG_META_DATA_XIAOMI_CONTINUITY_VERSION_NAME) != null
    }

    private fun buildNfcPayload(nfcDeviceType: NfcDeviceType, btMac: String, enableLyra: Boolean): ByteArray {
        val advData = MiConnectProto.AttrAdvData.newBuilder()
            .setVersionMajor(MI_CONNECT_MAJOR_VERSION)
            .setVersionMinor(MI_CONNECT_MINOR_VERSION)
            .setFlags(ByteString.copyFrom(byteArrayOf(MI_CONNECT_FLAG)))
            .setName(MI_CONNECT_NAME)
            .setDeviceType(MI_CONNECT_DEVICE_TYPE)
            .addAppIds(MI_CONNECT_APP_ID)
            .addAppsData(ByteString.copyFrom(buildNfcMirrorAppsData(nfcDeviceType, btMac, enableLyra)))
            .build()
        return MiConnectProto.AttrOps.newBuilder()
            .setAdvData(advData)
            .build()
            .toByteArray()
    }

    private fun buildNfcMirrorAppsData(nfcDeviceType: NfcDeviceType, btMac: String, enableLyra: Boolean): ByteArray =
        ByteArrayOutputStream().use {
            it.write(APPS_DATA_MAJOR_VERSION)
            it.write(APPS_DATA_MINOR_VERSION)
            it.writeInt32BigEndian(nfcDeviceType.value)
            it.write(0)
            it.write(ACTION_SUFFIX_TAG_DISCOVERED.length)
            it.write(ACTION_SUFFIX_TAG_DISCOVERED.toByteArray(Charsets.UTF_8))
            it.writeMap(buildNfcMirrorMap(btMac, enableLyra))
            it.toByteArray()
        }

    private fun buildNfcMirrorPayload(btMac: String, enableLyra: Boolean): ByteArray =
        ByteArrayOutputStream().use {
            it.writeMap(buildNfcMirrorMap(btMac, enableLyra))
            it.toByteArray()
        }

    private fun buildNfcMirrorMap(btMac: String, enableLyra: Boolean): Map<Int, BytesValue> = mutableMapOf(
        TAG_DISCOVERED_KEY_ACTION_SUFFIX to BytesValue(ACTION_SUFFIX_MIRROR),
        TAG_DISCOVERED_KEY_BT_MAC to BytesValue(btMac)
    ).also {
        if (enableLyra) {
            it[TAG_DISCOVERED_KEY_EXT_ABILITY] = BytesValue(FLAG_ABILITY_LYRA)
        }
    }

    private fun ByteArrayOutputStream.writeMap(map: Map<Int, BytesValue>) {
        for ((key, value) in map) {
            write(key)
            write(value.length)
            write(value.bytes)
        }
    }

    private fun ByteArrayOutputStream.writeInt32BigEndian(value: Int) {
        write(value shr 24)
        write(value shr 16)
        write(value shr 8)
        write(value shr 0)
    }

    @JvmInline
    private value class BytesValue(val bytes: ByteArray) {
        constructor(byte: Int) : this(byteArrayOf(byte.toByte()))
        constructor(str: String) : this(str.toByteArray(Charsets.UTF_8))

        val length: Int
            get() = bytes.size
    }

    enum class NfcDeviceType(val value: Int) {
        TV(2),
        PC(3),
        CAR(5),
        PAD(8)
    }
}