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

private fun <T> defaultKeyDelegate(defaultValue: T, block: (String) -> Preferences.Key<T>) =
    ReadOnlyProperty<Any, ReadWriteDefaultKey<T>> { _, property -> DefaultKey(block(property.name), defaultValue) }

fun booleanDefaultKey(name: String? = null, defaultValue: Boolean) =
    defaultKeyDelegate(defaultValue) { booleanPreferencesKey(name ?: it) }

fun stringDefaultKey(name: String? = null, defaultValue: String) =
    defaultKeyDelegate(defaultValue) { stringPreferencesKey(name ?: it) }

fun intDefaultKey(name: String? = null, defaultValue: Int) =
    defaultKeyDelegate(defaultValue) { intPreferencesKey(name ?: it) }

fun longDefaultKey(name: String? = null, defaultValue: Long) =
    defaultKeyDelegate(defaultValue) { longPreferencesKey(name ?: it) }

fun floatDefaultKey(name: String? = null, defaultValue: Float) =
    defaultKeyDelegate(defaultValue) { floatPreferencesKey(name ?: it) }

fun doubleDefaultKey(name: String? = null, defaultValue: Double) =
    defaultKeyDelegate(defaultValue) { doublePreferencesKey(name ?: it) }

fun stringSetDefaultKey(name: String? = null, defaultValue: Set<String>) =
    defaultKeyDelegate(defaultValue) { stringSetPreferencesKey(name ?: it) }

fun <T : Enum<T>> enumDefaultKey(name: String? = null, defaultValue: T, parser: (String) -> T) =
    ReadOnlyProperty<Any, ReadWriteDefaultEnumKey<T>> { _, property ->
        DefaultEnumKey(stringPreferencesKey(name ?: property.name), defaultValue, parser)
    }

fun <T : Enum<T>> enumSetDefaultKey(name: String? = null, defaultValue: Set<T>, parser: (String) -> T) =
    ReadOnlyProperty<Any, ReadWriteDefaultEnumSetKey<T>> { _, property ->
        DefaultEnumSetKey(stringSetPreferencesKey(name ?: property.name), defaultValue, parser)
    }

class DefaultKey<T>(preferencesKey: Preferences.Key<T>, private val defaultValue: T) : ReadWriteDefaultKey<T>(preferencesKey) {
    override fun defaultValue(): T = defaultValue
}

class DefaultEnumKey<T : Enum<T>>(
    preferencesKey: Preferences.Key<String>,
    private val defaultValue: T,
    private val parser: (String) -> T
) : ReadWriteDefaultEnumKey<T>(preferencesKey) {
    override fun defaultEnumValue(): T = defaultValue

    override suspend fun parseEnum(value: String): T = parser(value)
}

class DefaultEnumSetKey<T : Enum<T>>(
    preferencesKey: Preferences.Key<Set<String>>,
    private val defaultValue: Set<T>,
    private val parser: (String) -> T
) : ReadWriteDefaultEnumSetKey<T>(preferencesKey) {
    override fun defaultEnumSetValue(): Set<T> = defaultValue

    override suspend fun parseEnum(value: String): T = parser(value)
}
