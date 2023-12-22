@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package tool.xfy9326.milink.nfc.datastore.base.key

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import tool.xfy9326.milink.nfc.datastore.base.ExtendedDataStore


fun <T : Any> ExtendedDataStore.readValueFlow(key: ReadWriteKey<T>): Flow<T> =
    readFlow { key.getValue(it) }

suspend fun <T : Any> ExtendedDataStore.readValue(key: ReadWriteKey<T>): T =
    readValueFlow(key).first()


fun ExtendedDataStore.hasKeyFlow(key: ReadWriteKey<*>): Flow<Boolean> =
    readFlow { key.hasValue(it) }

suspend fun ExtendedDataStore.hasKey(key: ReadWriteKey<*>): Boolean =
    hasKeyFlow(key).first()


fun <T : Any> ExtendedDataStore.stateKey(
    key: ReadWriteKey<T>,
    coroutineScope: CoroutineScope
): StateFlow<T?> =
    readValueFlow(key).stateIn(coroutineScope, SharingStarted.WhileSubscribed(), null)

fun <D : ExtendedDataStore, T : Any> ExtendedDataStore.stateDefaultKey(
    key: ReadWriteDefaultKey<D, T>,
    coroutineScope: CoroutineScope
): StateFlow<T> =
    readValueFlow(key).stateIn(coroutineScope, SharingStarted.WhileSubscribed(), key.defaultValue())

fun ExtendedDataStore.stateHasKey(
    key: ReadWriteKey<*>,
    coroutineScope: CoroutineScope
): StateFlow<Boolean> =
    hasKeyFlow(key).stateIn(coroutineScope, SharingStarted.WhileSubscribed(), false)


suspend fun <T> ExtendedDataStore.writeValue(key: ReadWriteKey<T>, value: T) {
    edit { key.setValue(it, value) }
}

suspend fun ExtendedDataStore.removeKey(vararg keys: ReadWriteKey<*>) {
    edit { for (key in keys) key.removeValue(it) }
}

interface ReadWriteKey<T> {
    suspend fun hasValue(): Boolean

    suspend fun getValue(): T

    suspend fun setValue(value: T)

    suspend fun removeValue()


    fun hasValue(preferences: Preferences): Boolean

    suspend fun getValue(preferences: Preferences): T

    fun setValue(mutablePreferences: MutablePreferences, value: T)

    fun removeValue(mutablePreferences: MutablePreferences)
}

abstract class DataStoreReadWriteKey<D : ExtendedDataStore, T>(val dataStore: D) : ReadWriteKey<T> {
    override suspend fun hasValue(): Boolean = dataStore.read { hasValue(it) }

    override suspend fun getValue(): T = dataStore.read { getValue(it) }

    override suspend fun setValue(value: T) = dataStore.edit { setValue(it, value) }

    override suspend fun removeValue() = dataStore.edit { removeValue(it) }
}

abstract class ReadWriteDefaultKey<D : ExtendedDataStore, T>(
    dataStore: D,
    val preferencesKey: Preferences.Key<T>
) : DataStoreReadWriteKey<D, T>(dataStore) {
    abstract fun defaultValue(): T

    override fun hasValue(preferences: Preferences): Boolean = preferencesKey in preferences

    override suspend fun getValue(preferences: Preferences): T =
        preferences[preferencesKey] ?: defaultValue()

    override fun setValue(mutablePreferences: MutablePreferences, value: T) {
        mutablePreferences[preferencesKey] = value
    }

    override fun removeValue(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(preferencesKey)
    }
}

abstract class ReadWriteSuspendDefaultKey<D : ExtendedDataStore, T>(
    dataStore: D,
    val preferencesKey: Preferences.Key<T>
) : DataStoreReadWriteKey<D, T>(dataStore) {
    abstract suspend fun defaultValue(): T

    override fun hasValue(preferences: Preferences): Boolean = preferencesKey in preferences

    override suspend fun getValue(preferences: Preferences): T =
        preferences[preferencesKey] ?: defaultValue()

    override fun setValue(mutablePreferences: MutablePreferences, value: T) {
        mutablePreferences[preferencesKey] = value
    }

    override fun removeValue(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(preferencesKey)
    }
}

abstract class ReadWriteDefaultEnumKey<D : ExtendedDataStore, T : Enum<T>>(
    dataStore: D,
    preferencesKey: Preferences.Key<String>
) : ReadWriteDefaultKey<D, String>(dataStore, preferencesKey) {
    final override fun defaultValue(): String = defaultEnumValue().name

    final override suspend fun getValue(preferences: Preferences): String =
        super.getValue(preferences)

    final override fun setValue(mutablePreferences: MutablePreferences, value: String) =
        super.setValue(mutablePreferences, value)

    abstract fun defaultEnumValue(): T

    suspend fun getEnumValue(preferences: Preferences): T = parseEnum(getValue(preferences))

    protected abstract suspend fun parseEnum(value: String): T

    fun setEnumValue(mutablePreferences: MutablePreferences, value: T) {
        setValue(mutablePreferences, value.name)
    }
}

abstract class ReadWriteSuspendDefaultEnumKey<D : ExtendedDataStore, T : Enum<T>>(
    dataStore: D,
    preferencesKey: Preferences.Key<String>
) : ReadWriteSuspendDefaultKey<D, String>(dataStore, preferencesKey) {
    final override suspend fun defaultValue(): String = defaultEnumValue().name

    final override suspend fun getValue(preferences: Preferences): String =
        super.getValue(preferences)

    final override fun setValue(mutablePreferences: MutablePreferences, value: String) =
        super.setValue(mutablePreferences, value)

    abstract suspend fun defaultEnumValue(): T

    suspend fun getEnumValue(preferences: Preferences): T = parseEnum(getValue(preferences))

    protected abstract suspend fun parseEnum(value: String): T

    fun setEnumValue(mutablePreferences: MutablePreferences, value: T) {
        setValue(mutablePreferences, value.name)
    }
}

abstract class ReadWriteDefaultEnumSetKey<D : ExtendedDataStore, T : Enum<T>>(
    dataStore: D,
    preferencesKey: Preferences.Key<Set<String>>
) : ReadWriteDefaultKey<D, Set<String>>(dataStore, preferencesKey) {
    final override fun defaultValue(): Set<String> = defaultEnumSetValue().let {
        buildSet(it.size) { for (enum in it) add(enum.name) }
    }

    final override suspend fun getValue(preferences: Preferences): Set<String> =
        super.getValue(preferences)

    final override fun setValue(mutablePreferences: MutablePreferences, value: Set<String>) =
        super.setValue(mutablePreferences, value)

    abstract fun defaultEnumSetValue(): Set<T>

    suspend fun getEnumSetValue(preferences: Preferences): Set<T> =
        getValue(preferences).let { buildSet(it.size) { for (name in it) add(parseEnum(name)) } }

    protected abstract suspend fun parseEnum(value: String): T

    fun setEnumSetValue(mutablePreferences: MutablePreferences, value: Set<T>) {
        setValue(mutablePreferences, buildSet(value.size) { for (enum in value) add(enum.name) })
    }
}

abstract class ReadWriteSuspendDefaultEnumSetKey<D : ExtendedDataStore, T : Enum<T>>(
    dataStore: D,
    preferencesKey: Preferences.Key<Set<String>>
) : ReadWriteSuspendDefaultKey<D, Set<String>>(dataStore, preferencesKey) {
    final override suspend fun defaultValue(): Set<String> = defaultEnumSetValue().let {
        buildSet(it.size) { for (enum in it) add(enum.name) }
    }

    final override suspend fun getValue(preferences: Preferences): Set<String> =
        super.getValue(preferences)

    final override fun setValue(mutablePreferences: MutablePreferences, value: Set<String>) =
        super.setValue(mutablePreferences, value)

    abstract suspend fun defaultEnumSetValue(): Set<T>

    suspend fun getEnumSetValue(preferences: Preferences): Set<T> =
        getValue(preferences).let { buildSet(it.size) { for (name in it) add(parseEnum(name)) } }

    protected abstract suspend fun parseEnum(value: String): T

    fun setEnumSetValue(mutablePreferences: MutablePreferences, value: Set<T>) {
        setValue(mutablePreferences, buildSet(value.size) { for (enum in value) add(enum.name) })
    }
}
