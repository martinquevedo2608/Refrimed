package com.example.refrimed.data

data class RefrimedUiState(
    val queryState: QueryState = QueryState.IDLE,
    val btSend: String = "",
    val btResponse: String = "",
    val btState: QueryState = QueryState.OFFLINE,
    val wifiState: QueryState = QueryState.OFFLINE,
    val graph: MutableList<Values> = mutableListOf(Values()),
    val screen: Screen = Screen.HOME
)

enum class QueryState {
    LOADING,
    SUCCESS,
    ERROR,
    OFFLINE,
    IDLE
}

enum class Screen {
    HOME,
    BLUETOOTH,
    CLOUD,
    NOTES
}