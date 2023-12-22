@file:Suppress("unused", "SameParameterValue", "MemberVisibilityCanBePrivate")

package tool.xfy9326.milink.nfc.datastore.base

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import tool.xfy9326.milink.nfc.AppContext


abstract class ExtendedDataStore(name: String) {
    private val Context.datastore by preferencesDataStore(name)
    private val datastore by lazy { AppContext.datastore }

    fun <T> readFlow(block: suspend (Preferences) -> T): Flow<T> =
        datastore.data.map(block)

    suspend fun <T> read(block: suspend (Preferences) -> T): T =
        readFlow(block).first()


    fun hasKeyFlow(key: Preferences.Key<*>): Flow<Boolean> =
        readFlow { key in it }

    suspend fun hasKey(key: Preferences.Key<*>): Boolean =
        hasKeyFlow(key).first()


    fun <T : Any> readValueFlow(key: Preferences.Key<T>): Flow<T?> =
        readFlow { it[key] }

    fun <T : Any> readValueFlow(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
        readFlow { it[key] ?: defaultValue }

    fun <T : Any> readValueFlow(key: Preferences.Key<T>, defaultBlock: suspend () -> T): Flow<T> =
        readFlow { it[key] ?: defaultBlock() }

    suspend fun <T : Any> readValue(key: Preferences.Key<T>): T? =
        readValueFlow(key).first()

    suspend fun <T : Any> readValue(key: Preferences.Key<T>, defaultValue: T): T =
        readValueFlow(key, defaultValue).first()

    suspend fun <T : Any> readValue(key: Preferences.Key<T>, defaultBlock: suspend () -> T): T =
        readValueFlow(key, defaultBlock).first()


    fun <T : Any> stateKey(key: Preferences.Key<T>, coroutineScope: CoroutineScope): StateFlow<T?> =
        readValueFlow(key).stateIn(coroutineScope, SharingStarted.WhileSubscribed(), null)

    fun <T : Any> stateKey(
        key: Preferences.Key<T>,
        defaultValue: T,
        coroutineScope: CoroutineScope
    ): StateFlow<T> =
        readValueFlow(key, defaultValue).stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(),
            defaultValue
        )

    fun <T> statePreferences(
        block: suspend (Preferences) -> T,
        initialValue: T,
        coroutineScope: CoroutineScope
    ): StateFlow<T> =
        readFlow(block).stateIn(coroutineScope, SharingStarted.WhileSubscribed(), initialValue)

    fun stateHasKey(key: Preferences.Key<*>, coroutineScope: CoroutineScope): StateFlow<Boolean> =
        hasKeyFlow(key).stateIn(coroutineScope, SharingStarted.WhileSubscribed(), false)


    suspend fun <T> writeValue(key: Preferences.Key<T>, value: T) {
        datastore.edit { it[key] = value }
    }

    suspend fun edit(block: suspend (MutablePreferences) -> Unit) {
        datastore.edit(block)
    }

    suspend fun <T> edit(
        edit: suspend (MutablePreferences) -> Unit,
        read: suspend (Preferences) -> T
    ): T =
        read(datastore.edit(edit))


    suspend fun removeKey(vararg keys: Preferences.Key<*>) {
        datastore.edit { for (key in keys) it.remove(key) }
    }

    suspend fun clear() {
        datastore.edit { it.clear() }
    }
}
