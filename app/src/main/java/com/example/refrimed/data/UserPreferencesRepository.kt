package com.example.refrimed.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
){
    private companion object {
        val BLUETOOTH_DEVICE_CONFIG = stringPreferencesKey("bluetooth_device_config")
    }

    suspend fun saveConfig(bluetoothDeviceConfig: String) {
        dataStore.edit { preferences ->
            preferences[BLUETOOTH_DEVICE_CONFIG] = bluetoothDeviceConfig
        }
    }

    val deviceConfig: Flow<String> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e("UserPreferencesRepo", "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[BLUETOOTH_DEVICE_CONFIG] ?: ""
        }
}