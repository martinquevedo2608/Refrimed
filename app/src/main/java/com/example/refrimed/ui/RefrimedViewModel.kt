package com.example.refrimed.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.refrimed.data.RefrimedUiState
import com.example.refrimed.data.Screen
import com.example.refrimed.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class RefrimedViewModel(
    savedStateHandle: SavedStateHandle,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _refrimedUiState = MutableStateFlow(RefrimedUiState())
    val refrimedUiState: StateFlow<RefrimedUiState> = _refrimedUiState

    fun setScreen(screen: Screen) {
        _refrimedUiState.update { currentState ->
            currentState.copy(screen = screen)
        }
    }
}