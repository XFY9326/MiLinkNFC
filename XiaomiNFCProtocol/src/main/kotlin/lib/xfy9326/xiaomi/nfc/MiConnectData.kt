package lib.xfy9326.xiaomi.nfc

import com.google.protobuf.ByteString
import lib.xfy9326.xiaomi.nfc.proto.MiConnectProtocol

class MiConnectData private constructor(private val container: MiConnectProtocol.Container) {
    companion object {
        @Suppress("SpellCheckingInspection")
        private const val MI_CONNECT_PROTOCOL_PAYLOAD_NAME = "MI-NFCTAG"
        private const val MI_CONNECT_PROTOCOL_PAYLOAD_APP_ID = 16378
        private const val MI_CONNECT_PROTOCOL_PAYLOAD_DEVICE_TYPE = 15

        fun from(bytes: ByteArray): MiConnectData {
            return MiConnectData(MiConnectProtocol.Container.parseFrom(bytes))
        }

        fun from(payload: XiaomiNfcPayload<*>): MiConnectData {
            val miConnectPayload = MiConnectProtocol.Payload.newBuilder()
                .setVersionMajor(payload.majorVersion)
                .setVersionMinor(payload.minorVersion)
                .setFlags(ByteString.copyFrom(byteArrayOf(payload.protocol.flag)))
                .setName(MI_CONNECT_PROTOCOL_PAYLOAD_NAME)
                .setDeviceType(MI_CONNECT_PROTOCOL_PAYLOAD_DEVICE_TYPE)
                .addAppIds(MI_CONNECT_PROTOCOL_PAYLOAD_APP_ID)
                .addAppsData(ByteString.copyFrom(payload.appsData.encode()))
                .apply {
                    payload.idHash?.let {
                        this.setIdHash(ByteString.copyFrom(byteArrayOf(it)))
                    }
                }.build()
            val miConnectContainer = MiConnectProtocol.Container.newBuilder()
                .setData(miConnectPayload)
                .build()
            return MiConnectData(miConnectContainer)
        }
    }

    val isValidNfcPayload: Boolean =
        MI_CONNECT_PROTOCOL_PAYLOAD_APP_ID in container.data.appIdsList &&
                MI_CONNECT_PROTOCOL_PAYLOAD_DEVICE_TYPE == container.data.deviceType &&
                MI_CONNECT_PROTOCOL_PAYLOAD_NAME == container.data.name &&
                container.data.appsDataCount > 0

    fun getNfcProtocol(): XiaomiNfcProtocol<out AppsData> {
        require(isValidNfcPayload) { "Invalid MiConnectProtocol.Payload for NFC" }
        return XiaomiNfcProtocol.parse(container.data.flags.byteAt(0))
    }

    fun <T : AppsData> toXiaomiNfcPayload(protocol: XiaomiNfcProtocol<T>): XiaomiNfcPayload<T> {
        require(isValidNfcPayload) { "Invalid MiConnectProtocol.Payload for NFC" }
        val payloadProtocol = getNfcProtocol()
        require(payloadProtocol == protocol) { "Wrong protocol ${payloadProtocol::class}, excepted ${protocol::class}" }
        return XiaomiNfcPayload(
            majorVersion = container.data.versionMajor,
            minorVersion = container.data.versionMinor,
            idHash = container.data.idHash.toByteArray().firstOrNull(),
            protocol = protocol,
            appsData = protocol.decode(container.data.appsDataList.first().toByteArray())
        )
    }

    fun toByteArray(): ByteArray = container.toByteArray()
}
