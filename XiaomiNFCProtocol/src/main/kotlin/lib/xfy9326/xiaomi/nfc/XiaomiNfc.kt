@file:Suppress("unused")

package lib.xfy9326.xiaomi.nfc

import lib.xfy9326.xiaomi.nfc.proto.MiConnectProtocol

@Suppress("SpellCheckingInspection")
internal const val MI_CONNECT_PROTOCOL_PAYLOAD_NAME = "MI-NFCTAG"
internal const val MI_CONNECT_PROTOCOL_PAYLOAD_APP_ID = 16378
internal const val MI_CONNECT_PROTOCOL_PAYLOAD_DEVICE_TYPE = 15

fun ByteArray.decodeAsMiConnectPayload(): MiConnectProtocol.Payload =
    MiConnectProtocol.Container.parseFrom(this).data

fun MiConnectProtocol.Payload.isValidNfcPayload(): Boolean =
    MI_CONNECT_PROTOCOL_PAYLOAD_APP_ID in appIdsList &&
            MI_CONNECT_PROTOCOL_PAYLOAD_DEVICE_TYPE == deviceType &&
            MI_CONNECT_PROTOCOL_PAYLOAD_NAME == name &&
            appsDataCount > 0

fun MiConnectProtocol.Payload.getNfcProtocol(): XiaomiNfcProtocol<*> =
    XiaomiNfcProtocol.parse(flags.byteAt(0))

fun <T : AppsData> MiConnectProtocol.Payload.toXiaomiNfcPayload(protocol: XiaomiNfcProtocol<T>): XiaomiNfcPayload<T> {
    require(isValidNfcPayload()) { "Invalid MiConnectProtocol.Payload for NFC" }
    val payloadProtocol = getNfcProtocol()
    require(payloadProtocol == protocol) { "Wrong protocol ${payloadProtocol::class}, excepted ${protocol::class}" }
    return XiaomiNfcPayload(
        majorVersion = versionMajor,
        minorVersion = versionMinor,
        idHash = idHash.toByteArray().firstOrNull(),
        protocol = protocol,
        appsData = protocol.decode(appsDataList.first().toByteArray())
    )
}
