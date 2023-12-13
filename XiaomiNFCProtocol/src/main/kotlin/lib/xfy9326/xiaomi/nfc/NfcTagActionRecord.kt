package lib.xfy9326.xiaomi.nfc

import java.nio.ByteBuffer

data class NfcTagActionRecord(
    val action: Action,
    val condition: Condition,
    val deviceNumber: Byte,
    val flags: Byte,
    val conditionParameters: ByteArray
) : NfcTagRecord(TYPE_ACTION) {

    override fun encodeContent(): ByteArray {
        return ByteBuffer.allocate(
            Short.SIZE_BYTES + // action
                    Byte.SIZE_BYTES + // condition
                    Byte.SIZE_BYTES + // deviceNumber
                    Byte.SIZE_BYTES + // flags
                    conditionParameters.size // conditionParameters
        )
            .putShort(action.value)
            .put(condition.value)
            .put(deviceNumber)
            .put(flags)
            .put(conditionParameters)
            .array()
    }

    enum class Action(val value: Short) {
        UNKNOWN(0),
        IOT(1),
        MUSIC_RELAY(2),
        TEL_RELAY(3),
        FILE_TRANSFER(4),
        SCREEN_CASTING(5),
        CORP_OPERATION(6),
        VIDEO_RELAY(7),
        VOIP_RELAY(8),
        IOT_ENV(9),
        REMOTE_CONTROLLER(10),
        GUEST_NETWORK(11),
        EMPTY(12),
        CUSTOM(13),
        AUTO(Short.MAX_VALUE);

        companion object {
            private val valuesMap by lazy { entries.associateBy { it.value } }

            fun parse(value: Short) = valuesMap[value] ?: UNKNOWN
        }
    }

    enum class Condition(val value: Byte) {
        UNKNOWN(0),
        APP_FOREGROUND(1),
        SCREEN_LOCKED(2),
        AUTO(Byte.MAX_VALUE);

        companion object {
            private val valuesMap by lazy { entries.associateBy { it.value } }

            fun parse(value: Byte) = valuesMap[value] ?: UNKNOWN
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NfcTagActionRecord

        if (action != other.action) return false
        if (condition != other.condition) return false
        if (deviceNumber != other.deviceNumber) return false
        if (flags != other.flags) return false
        return conditionParameters.contentEquals(other.conditionParameters)
    }

    override fun hashCode(): Int {
        var result = action.hashCode()
        result = 31 * result + condition.hashCode()
        result = 31 * result + deviceNumber
        result = 31 * result + flags
        result = 31 * result + conditionParameters.contentHashCode()
        return result
    }
}
