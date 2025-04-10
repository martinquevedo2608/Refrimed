package com.example.refrimed.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.refrimed.RefrimedApplication

interface AppContainer{
    val userPreferencesRepository : UserPreferencesRepository
}

// DataStore con nombre "user_prefs"
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class AppDataContainer(private val context: Context) : AppContainer {

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.dataStore)
    }
}

fun CreationExtras.refrimedApplication(): RefrimedApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as RefrimedApplication)