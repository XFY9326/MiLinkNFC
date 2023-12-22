package tool.xfy9326.milink.nfc.utils

import java.io.Closeable

const val EMPTY = ""
const val SPACE = " "

const val MIME_ALL = "*/*"

inline fun <T : Closeable?, R> T.useCatching(block: T.() -> R): Result<R> =
    runCatching { use(block) }

private val macAddressRegex = "^([0-9A-Fa-f]{2}:){5}([0-9A-Fa-f]{2})\$".toRegex()

fun String.isValidMacAddress(): Boolean = macAddressRegex.matches(this)
