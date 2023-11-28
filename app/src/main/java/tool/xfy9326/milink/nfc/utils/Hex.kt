package tool.xfy9326.milink.nfc.utils

@OptIn(ExperimentalStdlibApi::class)
private val hexSeparateFormat = HexFormat {
    upperCase = true
    bytes {
        byteSeparator = ":"
    }
}

@OptIn(ExperimentalStdlibApi::class)
fun ByteArray.toHexString(separate: Boolean = false): String =
    toHexString(if (separate) hexSeparateFormat else HexFormat.Default)
