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
    DATETIME_RECEIVED,
    UPDATE_CHECKING,
    UPDATE_RECEIVED,
    UPDATE_UP_TO_DATE,
    UPDATE_AVAILABLE,
    UPDATE_UPDATING,
    UPDATE_OK,
    UPDATE_ERROR,
    UPDATE_ERROR_CHECK
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