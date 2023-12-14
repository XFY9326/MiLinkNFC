@file:Suppress("unused")

package tool.xfy9326.milink.nfc.datastore.base.key

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import tool.xfy9326.milink.nfc.datastore.base.ExtendedDataStore
import kotlin.properties.ReadOnlyProperty

private fun <D : ExtendedDataStore, T> defaultSuspendLazyKeyDelegate(defaultBlock: suspend () -> T, block: (String) -> Preferences.Key<T>) =
    ReadOnlyProperty<D, ReadWriteSuspendDefaultKey<D, T>> { obj, property -> SuspendDefaultLazyKey(obj, block(property.name), defaultBlock) }

fun <D : ExtendedDataStore> booleanSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> Boolean) =
    defaultSuspendLazyKeyDelegate<D, Boolean>(defaultBlock) { booleanPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> stringSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> String) =
    defaultSuspendLazyKeyDelegate<D, String>(defaultBlock) { stringPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> intSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> Int) =
    defaultSuspendLazyKeyDelegate<D, Int>(defaultBlock) { intPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> longSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> Long) =
    defaultSuspendLazyKeyDelegate<D, Long>(defaultBlock) { longPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> floatSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> Float) =
    defaultSuspendLazyKeyDelegate<D, Float>(defaultBlock) { floatPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> doubleSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> Double) =
    defaultSuspendLazyKeyDelegate<D, Double>(defaultBlock) { doublePreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> stringSetSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> Set<String>) =
    defaultSuspendLazyKeyDelegate<D, Set<String>>(defaultBlock) { stringSetPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore, T : Enum<T>> enumSuspendDefaultLazyKey(
    name: String? = null,
    defaultBlock: suspend () -> T,
    parser: (String) -> T
) = ReadOnlyProperty<D, ReadWriteSuspendDefaultEnumKey<D, T>> { obj, property ->
    SuspendDefaultEnumLazyKey(obj, stringPreferencesKey(name ?: property.name), defaultBlock, parser)
}

fun <D : ExtendedDataStore, T : Enum<T>> enumSetSuspendDefaultLazyKey(
    name: String? = null,
    defaultBlock: suspend () -> Set<T>,
    parser: (String) -> T
) = ReadOnlyProperty<D, ReadWriteSuspendDefaultEnumSetKey<D, T>> { obj, property ->
    SuspendDefaultEnumSetLazyKey(obj, stringSetPreferencesKey(name ?: property.name), defaultBlock, parser)
}


class SuspendDefaultLazyKey<D : ExtendedDataStore, T>(
    dataStore: D,
    preferencesKey: Preferences.Key<T>,
    private val defaultBlock: suspend () -> T
) : ReadWriteSuspendDefaultKey<D, T>(dataStore, preferencesKey) {
    override suspend fun defaultValue(): T = defaultBlock()
}

class SuspendDefaultEnumLazyKey<D : ExtendedDataStore, T : Enum<T>>(
    dataStore: D,
    preferencesKey: Preferences.Key<String>,
    private val defaultBlock: suspend () -> T,
    private val parser: (String) -> T
) : ReadWriteSuspendDefaultEnumKey<D, T>(dataStore, preferencesKey) {
    override suspend fun defaultEnumValue(): T = defaultBlock()

    override suspend fun parseEnum(value: String): T = parser(value)
}

class SuspendDefaultEnumSetLazyKey<D : ExtendedDataStore, T : Enum<T>>(
    dataStore: D,
    preferencesKey: Preferences.Key<Set<String>>,
    private val defaultBlock: suspend () -> Set<T>,
    private val parser: (String) -> T
) : ReadWriteSuspendDefaultEnumSetKey<D, T>(dataStore, preferencesKey) {
    override suspend fun defaultEnumSetValue(): Set<T> = defaultBlock()

    override suspend fun parseEnum(value: String): T = parser(value)
}