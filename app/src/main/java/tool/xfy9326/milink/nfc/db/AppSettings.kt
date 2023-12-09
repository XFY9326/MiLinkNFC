package tool.xfy9326.milink.nfc.db

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.BoolValue
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.milink.nfc.AppContext
import tool.xfy9326.milink.nfc.proto.AppSettingsProto
import tool.xfy9326.milink.nfc.proto.AppSettingsProto.MirrorIntent
import tool.xfy9326.milink.nfc.proto.AppSettingsProto.NfcDevice
import tool.xfy9326.milink.nfc.protocol.XiaomiMirrorNfc
import java.io.InputStream
import java.io.OutputStream

private const val DATASTORE_GLOBAL_NAME = "global.pb"

private val Context.globalDataStore by dataStore(DATASTORE_GLOBAL_NAME, GlobalSettingsSerializer())

private class GlobalSettingsSerializer : Serializer<AppSettingsProto.GlobalSettings> {
    override val defaultValue: AppSettingsProto.GlobalSettings = AppSettingsProto.GlobalSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AppSettingsProto.GlobalSettings {
        try {
            return AppSettingsProto.GlobalSettings.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto", e)
        }
    }

    override suspend fun writeTo(t: AppSettingsProto.GlobalSettings, output: OutputStream) {
        t.writeTo(output)
    }
}

object AppSettings {
    val global by lazy { AppContext.globalDataStore }

    object GlobalDefaults {
        val tilesNfcDevice = NfcDevice.PC
        val tilesMirrorIntent = MirrorIntent.MI_CONNECT_SERVICE
        val huaweiRedirectNfcDevice = NfcDevice.PC
        val huaweiRedirectMirrorIntent = MirrorIntent.FAKE_NFC_TAG
        const val tilesEnableLyra = true
        const val huaweiRedirectEnableLyra = true
    }

    suspend fun initValues(context: Context): Unit = withContext(Dispatchers.IO) {
        val isDeviceSupportLyra = XiaomiMirrorNfc.isLocalDeviceSupportLyra(context)

        global.updateData {
            it.toBuilder().apply {
                if (!it.hasTilesEnableLyra() && !isDeviceSupportLyra) {
                    tilesEnableLyra = BoolValue.of(false)
                }
                if (!it.hasHuaweiRedirectEnableLyra() && !isDeviceSupportLyra) {
                    huaweiRedirectEnableLyra = BoolValue.of(false)
                }
            }.build()
        }
    }
}
