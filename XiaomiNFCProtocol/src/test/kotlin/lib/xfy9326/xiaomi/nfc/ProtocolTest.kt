package lib.xfy9326.xiaomi.nfc

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalStdlibApi::class)
class ProtocolTest {
    companion object {
        private const val TEST_HEX_PAYLOAD_V1 =
            "0a63080110022201002a094d492d4e4643544147320100380f4a460100646e0c840002010036000300000001000600000000000000020006000000000000001200177869616f6d692e77696669737065616b65722e783038630200087fff7f00006a02fa7f"
        private const val TEST_HEX_PAYLOAD_V2 =
            "0a480801100b2201012a094d492d4e4643544147320100380f4a2b010063034f6b000201001b000300000001000611111111111000020006111111111111020008000d7f00006a02fa7f"
        private const val TEST_HEX_PAYLOAD_HANDOFF =
            "0a4b0801100d2201032a094d492d4e4643544147380f4a31271700000003000e5441475f444953434f564552454465064d4952524f52011130303a30303a30303a30303a30303a30306a02fa7f"

        private val TEST_PAYLOAD_V1 = NfcTagAppData(
            majorVersion = 1,
            minorVersion = 0,
            writeTime = 1684933764,
            flags = 0,
            records = listOf(
                NfcTagDeviceRecord.newInstance(
                    deviceType = NfcTagDeviceRecord.DeviceType.MI_SOUND_BOX,
                    flags = 0,
                    deviceNumber = 0,
                    attributesMap = mapOf(
                        NfcTagDeviceRecord.DeviceAttribute.WIFI_MAC_ADDRESS to ByteArray(6) { 0 },
                        NfcTagDeviceRecord.DeviceAttribute.BLUETOOTH_MAC_ADDRESS to ByteArray(6) { 0 },
                        NfcTagDeviceRecord.DeviceAttribute.MODEL to "xiaomi.wifispeaker.x08c".toByteArray(
                            Charsets.UTF_8
                        )
                    )
                ),
                NfcTagActionRecord.newInstance(
                    action = NfcTagActionRecord.Action.AUTO,
                    condition = NfcTagActionRecord.Condition.AUTO,
                    deviceNumber = 0,
                    flags = 0,
                    conditionParameters = ByteArray(0)
                )
            )
        )
        private val TEST_PAYLOAD_V2 = NfcTagAppData(
            majorVersion = 1,
            minorVersion = 0,
            writeTime = 1661161323,
            flags = 0,
            records = listOf(
                NfcTagDeviceRecord.newInstance(
                    deviceType = NfcTagDeviceRecord.DeviceType.MI_SOUND_BOX,
                    flags = 0,
                    deviceNumber = 0,
                    attributesMap = mapOf(
                        NfcTagDeviceRecord.DeviceAttribute.WIFI_MAC_ADDRESS to byteArrayOf(
                            0x11,
                            0x11,
                            0x11,
                            0x11,
                            0x11,
                            0x10
                        ),
                        NfcTagDeviceRecord.DeviceAttribute.BLUETOOTH_MAC_ADDRESS to byteArrayOf(
                            0x11,
                            0x11,
                            0x11,
                            0x11,
                            0x11,
                            0x11
                        ),
                    )
                ),
                NfcTagActionRecord.newInstance(
                    action = NfcTagActionRecord.Action.CUSTOM,
                    condition = NfcTagActionRecord.Condition.AUTO,
                    deviceNumber = 0,
                    flags = 0,
                    conditionParameters = ByteArray(0)
                )
            )
        )
        private val TEST_PAYLOAD_HANDOFF = HandoffAppData.newInstance(
            majorVersion = 0x27,
            minorVersion = 0x17,
            deviceType = HandoffAppData.DeviceType.PC,
            attributesMap = emptyMap(),
            action = "TAG_DISCOVERED",
            payloadsMap = mutableMapOf(
                HandoffAppData.PayloadKey.ACTION_SUFFIX to "MIRROR".toByteArray(Charsets.UTF_8),
                HandoffAppData.PayloadKey.BLUETOOTH_MAC to "00:00:00:00:00:00".toByteArray(Charsets.UTF_8)
            )
        )

        private inline fun <reified T : AppData> XiaomiNfcProtocol<T>.testProtocol(bytes: ByteArray): XiaomiNfcPayload<T> {
            val payload = MiConnectData.parse(bytes)

            assertTrue(payload.isValidNfcPayload)

            val protocol = payload.getNfcProtocol()
            assertEquals(this, protocol)

            val xiaomiNfcPayload = payload.toXiaomiNfcPayload(this)
            assertIs<T>(xiaomiNfcPayload.appData)

            return xiaomiNfcPayload
        }
    }


    @Test
    fun testV1Protocol() {
        val bytes = TEST_HEX_PAYLOAD_V1.hexToByteArray()
        val payload = XiaomiNfcProtocol.V1.testProtocol(bytes)
        assertContentEquals(bytes, MiConnectData.from(payload).toByteArray())
        assertContentEquals(TEST_PAYLOAD_V1.encode(), payload.appData.encode())
    }

    @Test
    fun testV2Protocol() {
        val bytes = TEST_HEX_PAYLOAD_V2.hexToByteArray()
        val payload = XiaomiNfcProtocol.V2.testProtocol(bytes)
        assertContentEquals(bytes, MiConnectData.from(payload).toByteArray())
        assertContentEquals(TEST_PAYLOAD_V2.encode(), payload.appData.encode())
    }

    @Test
    fun testHandoffProtocol() {
        val bytes = TEST_HEX_PAYLOAD_HANDOFF.hexToByteArray()
        val payload = XiaomiNfcProtocol.HandOff.testProtocol(bytes)
        assertContentEquals(bytes, MiConnectData.from(payload).toByteArray())
        assertContentEquals(TEST_PAYLOAD_HANDOFF.encode(), payload.appData.encode())
    }
}