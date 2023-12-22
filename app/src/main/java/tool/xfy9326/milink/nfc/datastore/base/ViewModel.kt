@file:Suppress("unused")

package tool.xfy9326.milink.nfc.datastore.base

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import tool.xfy9326.milink.nfc.datastore.base.key.ReadWriteDefaultKey
import tool.xfy9326.milink.nfc.datastore.base.key.ReadWriteKey
import tool.xfy9326.milink.nfc.datastore.base.key.stateDefaultKey
import tool.xfy9326.milink.nfc.datastore.base.key.stateHasKey
import tool.xfy9326.milink.nfc.datastore.base.key.stateKey

fun <D : ExtendedDataStore, T : Any> ViewModel.stateDataStoreKey(
    dataStore: D,
    key: Preferences.Key<T>
): StateFlow<T?> =
    dataStore.stateKey(key, viewModelScope)

fun <D : ExtendedDataStore, T : Any> ViewModel.stateDataStoreKey(
    dataStore: D,
    key: Preferences.Key<T>,
    defaultValue: T
): StateFlow<T> =
    dataStore.stateKey(key, defaultValue, viewModelScope)

fun <D : ExtendedDataStore, T> ViewModel.stateDataStore(
    dataStore: D,
    block: suspend (Preferences) -> T,
    initialValue: T
): StateFlow<T> =
    dataStore.statePreferences(block, initialValue, viewModelScope)

fun <D : ExtendedDataStore> ViewModel.stateHasKey(
    dataStore: D,
    key: Preferences.Key<*>
): StateFlow<Boolean> =
    dataStore.stateHasKey(key, viewModelScope)

fun <D : ExtendedDataStore, T : Any> ViewModel.stateDataStoreKey(
    dataStore: D,
    key: ReadWriteKey<T>
): StateFlow<T?> =
    dataStore.stateKey(key, viewModelScope)

fun <T : Any> ViewModel.stateDataStoreKey(key: ReadWriteDefaultKey<*, T>): StateFlow<T> =
    key.dataStore.stateDefaultKey(key, viewModelScope)

fun <D : ExtendedDataStore> ViewModel.stateHasKey(
    dataStore: D,
    key: ReadWriteKey<*>
): StateFlow<Boolean> =
    dataStore.stateHasKey(key, viewModelScope)

fun ViewModel.stateHasKey(key: ReadWriteDefaultKey<*, *>): StateFlow<Boolean> =
    key.dataStore.stateHasKey(key, viewModelScope)