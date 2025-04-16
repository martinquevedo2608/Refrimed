package com.example.refrimed.data

data class RefrimedUiState(
    val queryState: QueryState = QueryState.IDLE,
    val wifiState: QueryState = QueryState.OFFLINE,
    val screen: Screen = Screen.HOME
)

enum class QueryState {
    LOADING,
    SUCCESS,
    ERROR,
    OFFLINE,
    ONLINE,
    IDLE
}

enum class Screen {
    HOME,
    BLUETOOTH,
    CLOUD,
    NOTES,
    BLUETOOTH_DEVICE_CONFIG,
    BLUETOOTH_THRESHOLDS_CONFIG,
    BLUETOOTH_GRAFICO
}