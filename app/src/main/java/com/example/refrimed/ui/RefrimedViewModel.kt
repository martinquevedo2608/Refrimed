package com.example.refrimed.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.refrimed.data.QueryState
import com.example.refrimed.data.RefrimedUiState
import com.example.refrimed.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class RefrimedViewModel(
    savedStateHandle: SavedStateHandle,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RefrimedUiState())
    val uiState: StateFlow<RefrimedUiState> = _uiState

    fun setScreen(screen: com.example.refrimed.data.Screen) {
        _uiState.update { it.copy(screen = screen) }
    }

    fun setQueryState(queryState: QueryState) {
        _uiState.update { it.copy(queryState = queryState) }
    }

}