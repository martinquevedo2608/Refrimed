package com.example.refrimed.data

import java.util.Date

data class BtUiState(
    val connectionState: ConnectionState = ConnectionState.OFFLINE,
    val name: String = "",
    val logs: List<String> = listOf(""),
    val memoryUsed: String = "",
    val red: String = "",
    val pass: String = "",
    val temperatures: String = "Off",
    val currents: String = "Off",
    val relays: String = "Off",
    val alarms: String = "Off",
    val configReceived: Boolean = false,
    val configSent: Boolean = false,
    val configSaved: Boolean = false,
    val configQuery: QueryState = QueryState.IDLE,
    val actualDateTime: Date = Date(0),
    val actualTemperature1: Double = 0.0,
    val actualTemperature2: Double = 0.0,
    val actualTemperature3: Double = 0.0,
    val actualTemperature4: Double = 0.0,
    val actualCurrent: Double = 0.0,
    val actualRelay1: Boolean = false,
    val actualRelay2: Boolean = false,
    val actualAlarm1: Boolean = false,
    val actualAlarm2: Boolean = false,
    val actualAlarm3: Boolean = false,
    val graph: List<Values> = listOf(Values()),
    val thresholdsReceived: Boolean = false,
    val thresholdsSent: Boolean = false,
    val thresholdsSaved: Boolean = false,
    val thresholdsQuery: QueryState = QueryState.IDLE,
    val thresholdsAlarm1_mag: String = "",
    val thresholdsAlarm2_mag: String = "",
    val thresholdsAlarm3_mag: String = "",
    val thresholdsAlarm1_op: String = "",
    val thresholdsAlarm2_op: String = "",
    val thresholdsAlarm3_op: String = "",
    val thresholdsAlarm1_num: String = "",
    val thresholdsAlarm2_num: String = "",
    val thresholdsAlarm3_num: String = "",
    val thresholdsRelay1: String = "",
    val thresholdsRelay2: String = "",
    val recordReceived: Boolean = false,
    val recordQuery: QueryState = QueryState.IDLE
)

enum class ConnectionState {
    CONNECTED,
    OFFLINE,
    CONNECTING,
    ERROR
}