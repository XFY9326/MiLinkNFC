package lib.xfy9326.xiaomi.nfc

import com.google.protobuf.ByteString
import lib.xfy9326.xiaomi.nfc.proto.MiConnectProtocol

class MiConnectData private constructor(private val container: MiConnectProtocol.Container) {
    companion object {
        @Suppress("SpellCheckingInspection")
        private const val PAYLOAD_NAME = "MI-NFCTAG"
        private const val PAYLOAD_APP_ID = 16378
        private const val PAYLOAD_DEVICE_TYPE = 15

        fun parse(bytes: ByteArray): MiConnectData {
            return MiConnectData(MiConnectProtocol.Container.parseFrom(bytes))
        }

        fun from(payload: XiaomiNfcPayload<*>): MiConnectData {
            val miConnectPayload = MiConnectProtocol.Payload.newBuilder()
                .setVersionMajor(payload.majorVersion)
                .setVersionMinor(payload.minorVersion)
                .setFlags(ByteString.copyFrom(byteArrayOf(payload.protocol.flags)))
                .setName(PAYLOAD_NAME)
                .setDeviceType(PAYLOAD_DEVICE_TYPE)
                .addAppIds(PAYLOAD_APP_ID)
                .addAppsData(ByteString.copyFrom(payload.appData.encode()))
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
        PAYLOAD_APP_ID in container.data.appIdsList &&
                PAYLOAD_DEVICE_TYPE == container.data.deviceType &&
                PAYLOAD_NAME == container.data.name &&
                container.data.flags.size() > 0 &&
                container.data.appsDataCount > 0

    fun getNfcProtocol(): XiaomiNfcProtocol<out AppData> {
        require(isValidNfcPayload) { "Invalid MiConnectProtocol.Payload for NFC" }
        return XiaomiNfcProtocol.parse(container.data.flags.byteAt(0))
    }

    fun <T : AppData> toXiaomiNfcPayload(protocol: XiaomiNfcProtocol<T>): XiaomiNfcPayload<T> {
        require(isValidNfcPayload) { "Invalid MiConnectProtocol.Payload for NFC" }
        val payloadProtocol = getNfcProtocol()
        require(payloadProtocol == protocol) { "Wrong protocol ${payloadProtocol::class}, excepted ${protocol::class}" }
        return XiaomiNfcPayload(
            majorVersion = container.data.versionMajor,
            minorVersion = container.data.versionMinor,
            idHash = container.data.idHash.toByteArray().firstOrNull(),
            protocol = protocol,
            appData = protocol.decode(container.data.appsDataList.first().toByteArray())
        )
    }

    fun toByteArray(): ByteArray = container.toByteArray()
}
