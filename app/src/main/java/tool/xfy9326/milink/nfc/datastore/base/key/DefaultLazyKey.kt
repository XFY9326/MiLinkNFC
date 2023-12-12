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
import kotlin.properties.ReadOnlyProperty

private fun <T> defaultLazyKeyDelegate(defaultBlock: () -> T, block: (String) -> Preferences.Key<T>) =
    ReadOnlyProperty<Any, ReadWriteDefaultKey<T>> { _, property -> DefaultLazyKey(block(property.name), defaultBlock) }

fun booleanDefaultLazyKey(name: String? = null, defaultBlock: () -> Boolean) =
    defaultLazyKeyDelegate(defaultBlock) { booleanPreferencesKey(name ?: it) }

fun stringDefaultLazyKey(name: String? = null, defaultBlock: () -> String) =
    defaultLazyKeyDelegate(defaultBlock) { stringPreferencesKey(name ?: it) }

fun intDefaultLazyKey(name: String? = null, defaultBlock: () -> Int) =
    defaultLazyKeyDelegate(defaultBlock) { intPreferencesKey(name ?: it) }

fun longDefaultLazyKey(name: String? = null, defaultBlock: () -> Long) =
    defaultLazyKeyDelegate(defaultBlock) { longPreferencesKey(name ?: it) }

fun floatDefaultLazyKey(name: String? = null, defaultBlock: () -> Float) =
    defaultLazyKeyDelegate(defaultBlock) { floatPreferencesKey(name ?: it) }

fun doubleDefaultLazyKey(name: String? = null, defaultBlock: () -> Double) =
    defaultLazyKeyDelegate(defaultBlock) { doublePreferencesKey(name ?: it) }

fun stringSetDefaultLazyKey(name: String? = null, defaultBlock: () -> Set<String>) =
    defaultLazyKeyDelegate(defaultBlock) { stringSetPreferencesKey(name ?: it) }

fun <T : Enum<T>> enumDefaultLazyKey(name: String? = null, defaultBlock: () -> T, parser: (String) -> T) =
    ReadOnlyProperty<Any, ReadWriteSuspendDefaultEnumKey<T>> { _, property ->
        SuspendDefaultEnumLazyKey(stringPreferencesKey(name ?: property.name), defaultBlock, parser)
    }

fun <T : Enum<T>> enumSetDefaultLazyKey(name: String? = null, defaultBlock: () -> Set<T>, parser: (String) -> T) =
    ReadOnlyProperty<Any, ReadWriteSuspendDefaultEnumSetKey<T>> { _, property ->
        SuspendDefaultEnumSetLazyKey(stringSetPreferencesKey(name ?: property.name), defaultBlock, parser)
    }


class DefaultLazyKey<T>(
    preferencesKey: Preferences.Key<T>,
    private val defaultBlock: () -> T
) : ReadWriteDefaultKey<T>(preferencesKey) {
    override fun defaultValue(): T = defaultBlock()
}

class DefaultEnumLazyKey<T : Enum<T>>(
    preferencesKey: Preferences.Key<String>,
    private val defaultBlock: () -> T,
    private val parser: (String) -> T
) : ReadWriteDefaultEnumKey<T>(preferencesKey) {
    override fun defaultEnumValue(): T = defaultBlock()

    override suspend fun parseEnum(value: String): T = parser(value)
}

class DefaultEnumSetLazyKey<T : Enum<T>>(
    preferencesKey: Preferences.Key<Set<String>>,
    private val defaultBlock: () -> Set<T>,
    private val parser: (String) -> T
) : ReadWriteDefaultEnumSetKey<T>(preferencesKey) {
    override fun defaultEnumSetValue(): Set<T> = defaultBlock()

    override suspend fun parseEnum(value: String): T = parser(value)
}