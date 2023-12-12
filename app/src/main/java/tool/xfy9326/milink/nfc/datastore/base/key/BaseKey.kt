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

private fun <T> keyDelegate(block: (String) -> Preferences.Key<T>) =
    ReadOnlyProperty<Any, Preferences.Key<T>> { _, property -> block(property.name) }

fun booleanKey(name: String? = null) =
    keyDelegate { booleanPreferencesKey(name ?: it) }

fun stringKey(name: String? = null) =
    keyDelegate { stringPreferencesKey(name ?: it) }

fun intKey(name: String? = null) =
    keyDelegate { intPreferencesKey(name ?: it) }

fun longKey(name: String? = null) =
    keyDelegate { longPreferencesKey(name ?: it) }

fun floatKey(name: String? = null) =
    keyDelegate { floatPreferencesKey(name ?: it) }

fun doubleKey(name: String? = null) =
    keyDelegate { doublePreferencesKey(name ?: it) }

fun stringSetKey(name: String? = null) =
    keyDelegate { stringSetPreferencesKey(name ?: it) }
