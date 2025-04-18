package com.example.refrimed.ui

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.refrimed.data.BluetoothHelper
import com.example.refrimed.data.BtUiState
import com.example.refrimed.data.ConnectionState
import com.example.refrimed.data.QueryState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class BluetoothViewModel() : ViewModel() {

    private val _btState = MutableStateFlow(BtUiState())
    val btState: StateFlow<BtUiState> = _btState

    private val bluetoothHelper = BluetoothHelper(this) // Pasa la instancia al Helper
    val connectedSocket: StateFlow<BluetoothSocket?> = bluetoothHelper.connectedSocket
    val messagesFlow: SharedFlow<String> = bluetoothHelper.messagesFlow

    var thresholdsDeferred = CompletableDeferred<Boolean>()
    var configDeferred = CompletableDeferred<Boolean>()

    fun isBluetoothReady(): Boolean {
        return bluetoothHelper.isBluetoothAvailable() && bluetoothHelper.isBluetoothEnabled()
    }

    fun getPairedDevices(activity: Activity): List<BluetoothDevice> {
        return bluetoothHelper.getFilteredPairedDevices(activity)
    }

    fun connectTo(context: Context, device: BluetoothDevice) {
        bluetoothHelper.connectToDevice(context, device)
    }

    fun disconnect() {
        bluetoothHelper.closeConnection()
    }

    fun sendMessage(message: String) {
        bluetoothHelper.sendMessage(message)
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothHelper.closeConnection()
    }

    // Función para actualizar el uiState de forma segura
    fun updateBtState(transform: (BtUiState) -> BtUiState) {
        _btState.update(transform)
        Log.d("GraficLog", "${_btState.value.graph.size}")
    }

    fun setConnectionState(connectionState: ConnectionState) {
        _btState.update { it.copy(connectionState = connectionState) }
    }

    fun resetConfigStates() {
        _btState.update { it.copy(configReceived = false, configSaved = false, configQuery = QueryState.IDLE, configSent = false) }
    }

    fun resetThresholdsStates() {
        _btState.update { it.copy(thresholdsReceived = false, thresholdsSaved = false, thresholdsQuery = QueryState.IDLE, thresholdsSent = false) }
    }

    fun resetRecordStates() {
        _btState.update { it.copy(recordReceived = false, recordQuery = QueryState.IDLE) }
    }

    fun setConfigQueryState(queryState: QueryState) {
        _btState.update { it.copy(configQuery = queryState) }
    }

    fun setThresholdsQueryState(queryState: QueryState) {
        _btState.update { it.copy(thresholdsQuery = queryState) }
    }

    fun getDeviceConfig() {
        _btState.update { it.copy(configQuery = QueryState.CONFIG_ASKED) }
        sendMessage("get_config")
        CoroutineScope(Dispatchers.IO).launch {
            configDeferred = CompletableDeferred()
            val success = withTimeoutOrNull(3000) {
                configDeferred.await()
            }
            if (success == true) {
                if (_btState.value.configQuery == QueryState.CONFIG_ASKED) {
                    setConfigQueryState(QueryState.CONFIG_RECEIVED)
                }
            } else {
                setConfigQueryState(QueryState.ERROR)
            }
        }
    }

    fun getConfigState(): QueryState {
        return _btState.value.configQuery
    }

    fun getDeviceThresholds() {
        _btState.update { it.copy(thresholdsQuery = QueryState.THRESHOLDS_ASKED) }
        sendMessage("get_thresholds")
        CoroutineScope(Dispatchers.IO).launch {
            thresholdsDeferred = CompletableDeferred()
            val success = withTimeoutOrNull(3000) {
                thresholdsDeferred.await()
            }
            if (success == true) {
                if (_btState.value.thresholdsQuery == QueryState.THRESHOLDS_ASKED) {
                    setThresholdsQueryState(QueryState.THRESHOLDS_RECEIVED)
                }
            } else {
                setThresholdsQueryState(QueryState.ERROR)
            }
        }
    }

    fun getThresholdsState(): QueryState {
        return _btState.value.thresholdsQuery
    }

    fun getRecord(cantidad: Int = 0) {
        _btState.update { it.copy(recordReceived = false, recordQuery = QueryState.LOADING) }
        when (cantidad) {
            0 -> sendMessage("record_read")
            2*60*3 -> sendMessage("record_3hread")
            2*60*1 -> sendMessage("record_1hread")
            else -> { }
        }
    }

    fun getRecordErased() {
        _btState.update { it.copy(recordQuery = QueryState.ERASING) }
        sendMessage("record_erase")
    }

    fun getRecordErasedState(): QueryState {
        return _btState.value.recordQuery
    }

    fun setConfigSent() {
        _btState.update { it.copy(configSent = true, configQuery = QueryState.CONFIG_SENT) }
    }
    fun setThresholdsSent() {
        _btState.update { it.copy(thresholdsSent = true) }
    }

    fun setGraphState(queryState: QueryState) {
        _btState.update { it.copy(recordQuery = queryState) }
    }

    fun wifiConnect() {
        _btState.update { it.copy(wifiQuery = ConnectionState.CONNECTING) }
        sendMessage("wifi_conectar")
    }

    fun getWifiState(): ConnectionState {
        return _btState.value.wifiQuery
    }

    fun setWifiState(connectionState: ConnectionState) {
        _btState.update { it.copy(wifiQuery = connectionState) }
    }


}

fun List<String>.toCsv(): String = joinToString(",")
fun String.fromCsv(): List<String> = split(",")