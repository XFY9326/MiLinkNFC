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

fun String.hexToByteArray(separate: Boolean = false): ByteArray =
    hexToByteArray(if (separate) hexSeparateFormat else HexFormat.Default)

fun ByteArray.toHexString(separate: Boolean = false): String =
    toHexString(if (separate) hexSeparateFormat else HexFormat.UpperCase)

fun ByteArray.toHexText(): String =
    when (size) {
        0 -> EMPTY
        1 -> this[0].toHexString(true)
        else -> toHexString(true)
    }

fun Byte.toHexString(prefix: Boolean = false): String =
    toHexString(if (prefix) hexPrefixFormat else HexFormat.UpperCase)
