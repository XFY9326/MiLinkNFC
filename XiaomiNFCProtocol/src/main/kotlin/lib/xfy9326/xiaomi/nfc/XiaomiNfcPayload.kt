package lib.xfy9326.xiaomi.nfc

import com.google.protobuf.ByteString
import lib.xfy9326.xiaomi.nfc.proto.MiConnectProtocol

data class XiaomiNfcPayload<T : AppsData>(
    val majorVersion: Int,
    val minorVersion: Int,
    val idHash: Byte?,
    val protocol: XiaomiNfcProtocol<T>,
    val appsData: T
) : BinaryData {
    override fun encode(): ByteArray {
        val payload = MiConnectProtocol.Payload.newBuilder()
            .setVersionMajor(majorVersion)
            .setVersionMinor(minorVersion)
            .setFlags(ByteString.copyFrom(byteArrayOf(protocol.flag)))
            .setName(MI_CONNECT_PROTOCOL_PAYLOAD_NAME)
            .setDeviceType(MI_CONNECT_PROTOCOL_PAYLOAD_DEVICE_TYPE)
            .addAppIds(MI_CONNECT_PROTOCOL_PAYLOAD_APP_ID)
            .addAppsData(ByteString.copyFrom(appsData.encode()))
            .apply {
                this@XiaomiNfcPayload.idHash?.let { this.setIdHash(ByteString.copyFrom(byteArrayOf(it))) }
            }
            .build()
        return MiConnectProtocol.Container.newBuilder()
            .setData(payload)
            .build()
            .toByteArray()
    }
}