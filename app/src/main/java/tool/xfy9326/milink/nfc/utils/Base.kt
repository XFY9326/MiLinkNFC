package tool.xfy9326.milink.nfc.utils

const val EMPTY = ""
const val SPACE = " "

const val MIME_ALL = "*/*"

private val macAddressRegex = "^([0-9A-Fa-f]{2}:){5}([0-9A-Fa-f]{2})\$".toRegex()

fun String.isValidMacAddress(): Boolean = macAddressRegex.matches(this)
