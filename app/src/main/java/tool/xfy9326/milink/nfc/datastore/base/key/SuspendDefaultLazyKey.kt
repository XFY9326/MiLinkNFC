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

private fun <T> defaultSuspendLazyKeyDelegate(defaultBlock: suspend () -> T, block: (String) -> Preferences.Key<T>) =
    ReadOnlyProperty<Any, ReadWriteSuspendDefaultKey<T>> { _, property -> SuspendDefaultLazyKey(block(property.name), defaultBlock) }

fun booleanSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> Boolean) =
    defaultSuspendLazyKeyDelegate(defaultBlock) { booleanPreferencesKey(name ?: it) }

fun stringSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> String) =
    defaultSuspendLazyKeyDelegate(defaultBlock) { stringPreferencesKey(name ?: it) }

fun intSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> Int) =
    defaultSuspendLazyKeyDelegate(defaultBlock) { intPreferencesKey(name ?: it) }

fun longSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> Long) =
    defaultSuspendLazyKeyDelegate(defaultBlock) { longPreferencesKey(name ?: it) }

fun floatSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> Float) =
    defaultSuspendLazyKeyDelegate(defaultBlock) { floatPreferencesKey(name ?: it) }

fun doubleSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> Double) =
    defaultSuspendLazyKeyDelegate(defaultBlock) { doublePreferencesKey(name ?: it) }

fun stringSetSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> Set<String>) =
    defaultSuspendLazyKeyDelegate(defaultBlock) { stringSetPreferencesKey(name ?: it) }

fun <T : Enum<T>> enumSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> T, parser: (String) -> T) =
    ReadOnlyProperty<Any, ReadWriteSuspendDefaultEnumKey<T>> { _, property ->
        SuspendDefaultEnumLazyKey(stringPreferencesKey(name ?: property.name), defaultBlock, parser)
    }

fun <T : Enum<T>> enumSetSuspendDefaultLazyKey(name: String? = null, defaultBlock: suspend () -> Set<T>, parser: (String) -> T) =
    ReadOnlyProperty<Any, ReadWriteSuspendDefaultEnumSetKey<T>> { _, property ->
        SuspendDefaultEnumSetLazyKey(stringSetPreferencesKey(name ?: property.name), defaultBlock, parser)
    }


class SuspendDefaultLazyKey<T>(
    preferencesKey: Preferences.Key<T>,
    private val defaultBlock: suspend () -> T
) : ReadWriteSuspendDefaultKey<T>(preferencesKey) {
    override suspend fun defaultValue(): T = defaultBlock()
}

class SuspendDefaultEnumLazyKey<T : Enum<T>>(
    preferencesKey: Preferences.Key<String>,
    private val defaultBlock: suspend () -> T,
    private val parser: (String) -> T
) : ReadWriteSuspendDefaultEnumKey<T>(preferencesKey) {
    override suspend fun defaultEnumValue(): T = defaultBlock()

    override suspend fun parseEnum(value: String): T = parser(value)
}

class SuspendDefaultEnumSetLazyKey<T : Enum<T>>(
    preferencesKey: Preferences.Key<Set<String>>,
    private val defaultBlock: suspend () -> Set<T>,
    private val parser: (String) -> T
) : ReadWriteSuspendDefaultEnumSetKey<T>(preferencesKey) {
    override suspend fun defaultEnumSetValue(): Set<T> = defaultBlock()

    override suspend fun parseEnum(value: String): T = parser(value)
}