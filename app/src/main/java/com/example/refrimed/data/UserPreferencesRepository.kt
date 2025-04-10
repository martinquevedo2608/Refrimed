package com.example.refrimed.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
){
    private companion object {
        val REGISTERED_DEVICES = stringSetPreferencesKey("registered_devices")
    }

    suspend fun saveRegisteredDevices(registeredDevices: Set<String>) {
        dataStore.edit { preferences ->
            preferences[REGISTERED_DEVICES] = registeredDevices
        }
    }

    val registeredDevices: Flow<Set<String>> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e("UserPreferencesRepo", "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[REGISTERED_DEVICES] ?: emptySet() // Corregido aquí
        }
}