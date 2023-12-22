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

private fun <D : ExtendedDataStore, T> defaultKeyDelegate(
    defaultValue: T,
    block: (String) -> Preferences.Key<T>
) =
    ReadOnlyProperty<D, ReadWriteDefaultKey<D, T>> { obj, property ->
        DefaultKey(
            obj,
            block(property.name),
            defaultValue
        )
    }

fun <D : ExtendedDataStore> booleanDefaultKey(name: String? = null, defaultValue: Boolean) =
    defaultKeyDelegate<D, Boolean>(defaultValue) { booleanPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> stringDefaultKey(name: String? = null, defaultValue: String) =
    defaultKeyDelegate<D, String>(defaultValue) { stringPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> intDefaultKey(name: String? = null, defaultValue: Int) =
    defaultKeyDelegate<D, Int>(defaultValue) { intPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> longDefaultKey(name: String? = null, defaultValue: Long) =
    defaultKeyDelegate<D, Long>(defaultValue) { longPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> floatDefaultKey(name: String? = null, defaultValue: Float) =
    defaultKeyDelegate<D, Float>(defaultValue) { floatPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> doubleDefaultKey(name: String? = null, defaultValue: Double) =
    defaultKeyDelegate<D, Double>(defaultValue) { doublePreferencesKey(name ?: it) }

fun <D : ExtendedDataStore> stringSetDefaultKey(name: String? = null, defaultValue: Set<String>) =
    defaultKeyDelegate<D, Set<String>>(defaultValue) { stringSetPreferencesKey(name ?: it) }

fun <D : ExtendedDataStore, T : Enum<T>> enumDefaultKey(
    name: String? = null,
    defaultValue: T,
    parser: (String) -> T
) =
    ReadOnlyProperty<D, ReadWriteDefaultEnumKey<D, T>> { obj, property ->
        DefaultEnumKey(obj, stringPreferencesKey(name ?: property.name), defaultValue, parser)
    }

fun <D : ExtendedDataStore, T : Enum<T>> enumSetDefaultKey(
    name: String? = null,
    defaultValue: Set<T>,
    parser: (String) -> T
) =
    ReadOnlyProperty<D, ReadWriteDefaultEnumSetKey<D, T>> { obj, property ->
        DefaultEnumSetKey(obj, stringSetPreferencesKey(name ?: property.name), defaultValue, parser)
    }

class DefaultKey<D : ExtendedDataStore, T>(
    dataStore: D,
    preferencesKey: Preferences.Key<T>,
    private val defaultValue: T
) : ReadWriteDefaultKey<D, T>(dataStore, preferencesKey) {
    override fun defaultValue(): T = defaultValue
}

class DefaultEnumKey<D : ExtendedDataStore, T : Enum<T>>(
    dataStore: D,
    preferencesKey: Preferences.Key<String>,
    private val defaultValue: T,
    private val parser: (String) -> T
) : ReadWriteDefaultEnumKey<D, T>(dataStore, preferencesKey) {
    override fun defaultEnumValue(): T = defaultValue

    override suspend fun parseEnum(value: String): T = parser(value)
}

class DefaultEnumSetKey<D : ExtendedDataStore, T : Enum<T>>(
    dataStore: D,
    preferencesKey: Preferences.Key<Set<String>>,
    private val defaultValue: Set<T>,
    private val parser: (String) -> T
) : ReadWriteDefaultEnumSetKey<D, T>(dataStore, preferencesKey) {
    override fun defaultEnumSetValue(): Set<T> = defaultValue

    override suspend fun parseEnum(value: String): T = parser(value)
}
