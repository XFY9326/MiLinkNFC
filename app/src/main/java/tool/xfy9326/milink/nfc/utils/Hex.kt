@file:OptIn(ExperimentalStdlibApi::class)

package tool.xfy9326.milink.nfc.utils

private val hexSeparateFormat = HexFormat {
    upperCase = true
    bytes {
        byteSeparator = ":"
    }
}

private val hexPrefixFormat = HexFormat {
    upperCase = true
    number {
        prefix = "0x"
    }
}

fun ByteArray.toHexString(separate: Boolean = false): String =
    toHexString(if (separate) hexSeparateFormat else HexFormat.UpperCase)

fun Byte.toHexString(prefix: Boolean = false): String =
    toHexString(if (prefix) hexPrefixFormat else HexFormat.UpperCase)
