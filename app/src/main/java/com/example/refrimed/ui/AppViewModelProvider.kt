package com.example.refrimed.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.refrimed.RefrimedApplication

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            RefrimedViewModel(
                createSavedStateHandle(),
                refrimedApplication().container.userPreferencesRepository
            )
        }
        initializer {
            BluetoothViewModel(
                createSavedStateHandle(),
                refrimedApplication().container.userPreferencesRepository
            )
        }
    }
}

// Extension function para obtener la instancia de la Application
fun CreationExtras.refrimedApplication(): RefrimedApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RefrimedApplication)
