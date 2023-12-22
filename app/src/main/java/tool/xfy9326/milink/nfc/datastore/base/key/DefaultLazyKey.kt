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

private fun <D : ExtendedDataStore, T> defaultLazyKeyDelegate(
    defaultBlock: () -> T,
    block: (String) -> Preferences.Key<T>
) =
    ReadOnlyProperty<D, ReadWriteDefaultKey<D, T>> { obj, property ->
        DefaultLazyKey(
            obj,
            block(property.name),
            defaultBlock
        )
    }

fun <D : ExtendedDataStore> booleanDefaultLazyKey(
    name: String? = null,
    defaultBlock: () -> Boolean
) =
    defaultLazyKeyDelegate<D, Boolean>(defaultBlock) { booleanPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> stringDefaultLazyKey(name: String? = null, defaultBlock: () -> String) =
    defaultLazyKeyDelegate<D, String>(defaultBlock) { stringPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> intDefaultLazyKey(name: String? = null, defaultBlock: () -> Int) =
    defaultLazyKeyDelegate<D, Int>(defaultBlock) { intPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> longDefaultLazyKey(name: String? = null, defaultBlock: () -> Long) =
    defaultLazyKeyDelegate<D, Long>(defaultBlock) { longPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> floatDefaultLazyKey(name: String? = null, defaultBlock: () -> Float) =
    defaultLazyKeyDelegate<D, Float>(defaultBlock) { floatPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> doubleDefaultLazyKey(name: String? = null, defaultBlock: () -> Double) =
    defaultLazyKeyDelegate<D, Double>(defaultBlock) { doublePreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> stringSetDefaultLazyKey(
    name: String? = null,
    defaultBlock: () -> Set<String>
) =
    defaultLazyKeyDelegate<D, Set<String>>(defaultBlock) { stringSetPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore, T : Enum<T>> enumDefaultLazyKey(
    name: String? = null,
    defaultBlock: () -> T,
    parser: (String) -> T
) =
    ReadOnlyProperty<D, ReadWriteDefaultEnumKey<D, T>> { obj, property ->
        DefaultEnumLazyKey(obj, stringPreferencesKey(name ?: property.name), defaultBlock, parser)
    }

fun <D : ExtendedDataStore, T : Enum<T>> enumSetDefaultLazyKey(
    name: String? = null,
    defaultBlock: () -> Set<T>,
    parser: (String) -> T
) =
    ReadOnlyProperty<D, ReadWriteDefaultEnumSetKey<D, T>> { obj, property ->
        DefaultEnumSetLazyKey(
            obj,
            stringSetPreferencesKey(name ?: property.name),
            defaultBlock,
            parser
        )
    }


class DefaultLazyKey<D : ExtendedDataStore, T>(
    dataStore: D,
    preferencesKey: Preferences.Key<T>,
    private val defaultBlock: () -> T
) : ReadWriteDefaultKey<D, T>(dataStore, preferencesKey) {
    override fun defaultValue(): T = defaultBlock()
}

class DefaultEnumLazyKey<D : ExtendedDataStore, T : Enum<T>>(
    dataStore: D,
    preferencesKey: Preferences.Key<String>,
    private val defaultBlock: () -> T,
    private val parser: (String) -> T
) : ReadWriteDefaultEnumKey<D, T>(dataStore, preferencesKey) {
    override fun defaultEnumValue(): T = defaultBlock()

    override suspend fun parseEnum(value: String): T = parser(value)
}

class DefaultEnumSetLazyKey<D : ExtendedDataStore, T : Enum<T>>(
    dataStore: D,
    preferencesKey: Preferences.Key<Set<String>>,
    private val defaultBlock: () -> Set<T>,
    private val parser: (String) -> T
) : ReadWriteDefaultEnumSetKey<D, T>(dataStore, preferencesKey) {
    override fun defaultEnumSetValue(): Set<T> = defaultBlock()

    override suspend fun parseEnum(value: String): T = parser(value)
}