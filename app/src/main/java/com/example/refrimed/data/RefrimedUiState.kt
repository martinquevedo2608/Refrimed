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
    IDLE,
    ERASED,
    ERASING,
    CONFIG_ASKED,
    CONFIG_RECEIVED,
    CONFIG_SENT,
    CONFIG_OK,
    THRESHOLDS_ASKED,
    THRESHOLDS_RECEIVED,
    THRESHOLDS_SENT,
    THRESHOLDS_OK,
    RECORD_ASKED,
    RECORD_RECEIVED,
    DATETIME_SENT,
    DATETIME_RECEIVED
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