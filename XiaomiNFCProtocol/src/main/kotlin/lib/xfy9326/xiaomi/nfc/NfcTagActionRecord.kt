package lib.xfy9326.xiaomi.nfc

import java.nio.ByteBuffer

data class NfcTagActionRecord(
    val action: Short,
    val condition: Byte,
    val deviceNumber: Byte,
    val flags: Byte,
    val conditionParameters: ByteArray? = null
) : NfcTagRecord(TYPE_ACTION) {

    companion object {
        fun newInstance(
            action: Action,
            condition: Condition,
            deviceNumber: Byte,
            flags: Byte,
            conditionParameters: ByteArray? = null
        ) = NfcTagActionRecord(
            action = action.value,
            condition = condition.value,
            deviceNumber = deviceNumber,
            flags = flags,
            conditionParameters = conditionParameters
        )
    }

    val enumAction by lazy { Action.parse(action) }
    val enumCondition by lazy { Condition.parse(condition) }

    override fun contentSize(): Int {
        return Short.SIZE_BYTES + // action
                Byte.SIZE_BYTES + // condition
                Byte.SIZE_BYTES + // deviceNumber
                Byte.SIZE_BYTES + // flags
                (conditionParameters?.size ?: 0) // conditionParameters
    }

    override fun encodeContentInto(buffer: ByteBuffer) {
        buffer.putShort(action)
            .put(condition)
            .put(deviceNumber)
            .put(flags)
            .apply {
                conditionParameters?.let { put(it) }
            }
    }

    enum class Action(val value: Short) {
        UNKNOWN(-1),
        BEGIN(0),
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
        END(14),
        AUTO(Short.MAX_VALUE);

        companion object {
            private val valuesMap by lazy { entries.associateBy { it.value } }

            fun parse(value: Short) = valuesMap[value] ?: UNKNOWN
        }
    }

    enum class Condition(val value: Byte) {
        UNKNOWN(-1),
        BEGIN(0),
        APP_FOREGROUND(1),
        SCREEN_LOCKED(2),
        END(3),
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
        if (conditionParameters != null) {
            if (other.conditionParameters == null) return false
            if (!conditionParameters.contentEquals(other.conditionParameters)) return false
        } else if (other.conditionParameters != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = action.toInt()
        result = 31 * result + condition
        result = 31 * result + deviceNumber
        result = 31 * result + flags
        result = 31 * result + (conditionParameters?.contentHashCode() ?: 0)
        return result
    }
}
