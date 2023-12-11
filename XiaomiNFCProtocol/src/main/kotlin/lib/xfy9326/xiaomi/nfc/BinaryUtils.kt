package lib.xfy9326.xiaomi.nfc

import java.nio.ByteBuffer

internal fun List<ByteArray>.totalBytes(): Int =
    fold(0) { acc, bytes -> acc + bytes.size }

internal fun Map<Byte, ByteArray>.bytesMapTotalBytes(): Int =
    entries.fold(0) { acc, entry -> acc + Byte.SIZE_BYTES * 2 + entry.value.size }

internal fun Map<Short, ByteArray>.shortMapTotalBytes(): Int =
    entries.fold(0) { acc, entry -> acc + Short.SIZE_BYTES * 2 + entry.value.size }

internal fun ByteBuffer.putByteArrays(array: Iterable<ByteArray>): ByteBuffer {
    for (bytes in array) put(bytes)
    return this
}

internal fun ByteBuffer.putByteKeyBytesMap(map: Map<Byte, ByteArray>): ByteBuffer {
    for ((key, value) in map) {
        put(key)
        put(value.size.toByte())
        put(value)
    }
    return this
}

internal fun ByteBuffer.putShortKeyBytesMap(map: Map<Short, ByteArray>): ByteBuffer {
    for ((key, value) in map) {
        putShort(key)
        putShort(value.size.toShort())
        put(value)
    }
    return this
}

internal fun ByteBuffer.getByteKeyBytesMap(size: Int? = null): Map<Byte, ByteArray> = buildMap {
    var i = 0
    while ((size == null || i < size) && hasRemaining()) {
        val key = this@getByteKeyBytesMap.get()
        val valueSize = this@getByteKeyBytesMap.get().toInt()
        val value = ByteArray(valueSize).also { this@getByteKeyBytesMap.get(it) }
        this[key] = value
        i++
    }
}

internal fun ByteBuffer.getShortKeyBytesMap(size: Int? = null): Map<Short, ByteArray> = buildMap {
    var i = 0
    while ((size == null || i < size) && hasRemaining()) {
        val key = this@getShortKeyBytesMap.getShort()
        val valueSize = this@getShortKeyBytesMap.getShort().toInt()
        val value = ByteArray(valueSize).also { this@getShortKeyBytesMap.get(it) }
        this[key] = value
        i++
    }
}