package com.example.refrimed.ui

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.refrimed.data.refrimedApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            RefrimedViewModel(
                this.createSavedStateHandle(),
                refrimedApplication().container.userPreferencesRepository // Pasamos el userPreferencesRepository
            )
        }
    }
}