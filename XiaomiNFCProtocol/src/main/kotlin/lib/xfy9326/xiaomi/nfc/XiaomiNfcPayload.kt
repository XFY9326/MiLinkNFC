package lib.xfy9326.xiaomi.nfc

data class XiaomiNfcPayload<T : AppsData>(
    val majorVersion: Int,
    val minorVersion: Int,
    val idHash: Byte?,
    val protocol: XiaomiNfcProtocol<T>,
    val appsData: T
)