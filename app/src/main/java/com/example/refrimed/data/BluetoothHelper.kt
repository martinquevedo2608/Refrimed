package com.example.refrimed.data

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.refrimed.ui.BluetoothViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BluetoothHelper(
    private val btViewModel: BluetoothViewModel // Recibe la instancia del ViewModel
) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // UUID estándar para SPP

    private val _connectedSocket = MutableStateFlow<BluetoothSocket?>(null)
    val connectedSocket: StateFlow<BluetoothSocket?> = _connectedSocket

    private val _messagesFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val messagesFlow: SharedFlow<String> = _messagesFlow

    private var badCount = 0
    private var recordList = mutableListOf<Values>()

    fun isBluetoothAvailable(): Boolean = bluetoothAdapter != null
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun getPairedDevices(activity: Activity): List<BluetoothDevice> {
        if (!hasPermission(activity)) return emptyList()
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(context: Context, device: BluetoothDevice) {
        if (!hasPermission(context)) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothAdapter?.cancelDiscovery()
                socket.connect()
                _connectedSocket.value = socket
                listenForMessages(socket)
                monitorDisconnection(socket)
                Log.i("BluetoothHelper", "Conectado a ${device.name}")
                btViewModel.getDeviceConfig()
            } catch (e: IOException) {
                Log.e("BluetoothHelper", "Connection failed to ${device.name}: ${e.message}")
                _connectedSocket.value = null
                btViewModel.updateBtState { it.copy( connectionState = ConnectionState.ERROR ) }
            }
        }
    }

    fun sendMessage(message: String) {
        val socket = _connectedSocket.value
        if (socket != null && socket.isConnected) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    socket.outputStream.write((message + "\n").toByteArray())
                    socket.outputStream.flush()
                    Log.d("BluetoothHelper", "Mensaje enviado: $message")
                } catch (e: IOException) {
                    Log.e("BluetoothHelper", "Error enviando mensaje: ${e.message}")
                }
            }
        } else {
            Log.w("BluetoothHelper", "No se puede enviar el mensaje. Socket no conectado.")
        }
    }

    private fun listenForMessages(socket: BluetoothSocket) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = socket.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))

                while (true) {
                    var message = reader.readLine()  // lee hasta \n
                    if (message != null) {
                        message = message.trim()
                        Log.d("BluetoothReceiver", "Mensaje recibido: $message")
                        _messagesFlow.tryEmit(message) // sigue disponible para que alguien lo consuma si quiere
                        parseAndUpdate(message) // actualiza el ViewModel si corresponde
                    } else {
                        Log.d("BluetoothReceiver", "Stream cerrado por el otro extremo.")
                        break
                    }
                }
            } catch (e: IOException) {
                Log.e("BluetoothReceiver", "Error leyendo datos", e)
            }
        }
    }



    private fun parseAndUpdate(message: String) {
        val parts = message.split(",")
        if (parts.size == 13 && parts[0] == "VALUES") { //Valores actuales frecuentes
            try {
                val timestampString = parts[1].trim()
                val temperature1 = parts[2].trim().toDoubleOrNull() ?: 0.0
                val temperature2 = parts[3].trim().toDoubleOrNull() ?: 0.0
                val temperature3 = parts[4].trim().toDoubleOrNull() ?: 0.0
                val temperature4 = parts[5].trim().toDoubleOrNull() ?: 0.0
                val current = parts[6].trim().toDoubleOrNull() ?: 0.0
                val relay1 = parts[7].trim() == "1"
                val relay2 = parts[8].trim() == "1"
                val alarm1 = parts[9].trim() == "1"
                val alarm2 = parts[10].trim() == "1"
                val alarm3 = parts[11].trim() == "1"
                val wifiState = if (parts[12].trim() == "1") ConnectionState.ONLINE else ConnectionState.OFFLINE

                val dateTimeFormat =
                    SimpleDateFormat("dd/MM/yy - HH:mm:ss", Locale.getDefault())
                val parsedDateTime = dateTimeFormat.parse(timestampString)
                val actualDateTime = parsedDateTime ?: Date(0)

                btViewModel.updateBtState { currentState ->
                    currentState.copy(
                        actualDateTime = actualDateTime,
                        actualTemperature1 = temperature1,
                        actualTemperature2 = temperature2,
                        actualTemperature3 = temperature3,
                        actualTemperature4 = temperature4,
                        actualCurrent = current,
                        actualRelay1 = relay1,
                        actualRelay2 = relay2,
                        actualAlarm1 = alarm1,
                        actualAlarm2 = alarm2,
                        actualAlarm3 = alarm3,
                        wifiQuery = wifiState
                    )
                }

                if (btViewModel.getWifiState() == ConnectionState.OFFLINE) {

                }

            } catch (e: NumberFormatException) {
                Log.e(
                    TAG,
                    "Error al parsear valores numéricos: ${e.message}, mensaje: $message"
                )
            } catch (e: StringIndexOutOfBoundsException) {
                Log.e(
                    TAG,
                    "Error al extraer la hora del timestamp: ${e.message}, mensaje: $message"
                )
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error al parsear el mensaje VALUES: ${e.message}, mensaje: $message"
                )
            }
        } else if (parts.size == 7 && parts[0] == "CONFIG") { //recibo Config del equipo
            try {
                btViewModel.updateBtState { currentState ->
                    currentState.copy(
                        red = parts[1].trim(),
                        pass = parts[2].trim(),
                        temperatures = parts[3].trim(),
                        currents = parts[4].trim(),
                        relays = parts[5].trim(),
                        alarms = parts[6].trim(),
                        configReceived = true
                    )
                }

                if (btViewModel.getConfigState() == QueryState.CONFIG_ASKED) {
                    btViewModel.updateBtState { it.copy(configQuery = QueryState.CONFIG_RECEIVED) }
                }

                try { btViewModel.configDeferred.complete(true) } catch (_: Exception) { }
            } catch (e: Exception) { Log.e(TAG, "Error al parsear el mensaje CONFIG: ${e.message}, mensaje: $message") }
        } else if (parts.size == 12 && parts[0] == "THRESHOLDS") { //recibo Thresholds del equipo
            try {
                btViewModel.updateBtState { currentState ->
                    currentState.copy(
                        thresholdsAlarm1_mag = parts[1].trim(),
                        thresholdsAlarm1_op = parts[2].trim(),
                        thresholdsAlarm1_num = parts[3].trim(),
                        thresholdsAlarm2_mag = parts[4].trim(),
                        thresholdsAlarm2_op = parts[5].trim(),
                        thresholdsAlarm2_num = parts[6].trim(),
                        thresholdsAlarm3_mag = parts[7].trim(),
                        thresholdsAlarm3_op = parts[8].trim(),
                        thresholdsAlarm3_num = parts[9].trim(),
                        thresholdsRelay1 = parts[10].trim(),
                        thresholdsRelay2 = parts[11].trim(),
                        thresholdsReceived = true
                    )
                }

                if (btViewModel.getThresholdsState() == QueryState.THRESHOLDS_ASKED) {
                    btViewModel.updateBtState { it.copy(thresholdsQuery = QueryState.THRESHOLDS_RECEIVED) }
                }

                try { btViewModel.thresholdsDeferred.complete(true) } catch (_: Exception) { }
            } catch (e: Exception) { Log.e(TAG, "Error al parsear el mensaje THRESHOLDS: ${e.message}, mensaje: $message") }
        } else if (parts.size == 12 && parts[0] in listOf("RSTART", "RE", "RFINISH")) { //recibo Registros del equipo
            if (parts[0] == "RSTART") {
                badCount = 0;
                recordList = mutableListOf()
            } else if (parts[0] == "RE") {
                val timestampString = parts[1].trim()
                val dateTimeFormat = SimpleDateFormat("dd/MM/yy - HH:mm:ss", Locale.getDefault())
                try {
                    val parsedTimestamp = dateTimeFormat.parse(timestampString)

                    recordList.add(
                        Values (
                            timestamp = parsedTimestamp ?: Date(), // Usa la fecha parseada o la actual si falla
                            temp1 = parts[2].trim().toDoubleOrNull() ?: 0.0,
                            temp2 = parts[3].trim().toDoubleOrNull() ?: 0.0,
                            temp3 = parts[4].trim().toDoubleOrNull() ?: 0.0,
                            temp4 = parts[5].trim().toDoubleOrNull() ?: 0.0,
                            current = parts[6].trim().toDoubleOrNull() ?: 0.0,
                            relay1 = parts[7].trim().toBooleanStrictOrNull() ?: false,
                            relay2 = parts[8].trim().toBooleanStrictOrNull() ?: false,
                            alarm1 = parts[8].trim().toBooleanStrictOrNull() ?: false,
                            alarm2 = parts[8].trim().toBooleanStrictOrNull() ?: false,
                            alarm3 = parts[8].trim().toBooleanStrictOrNull() ?: false
                        )
                    )

                    try { btViewModel.thresholdsDeferred.complete(true) } catch (_: Exception) { }
                } catch (e: Exception) { Log.e( TAG, "Error al parsear el mensaje RECORD: ${e.message}, mensaje: $message" ); badCount++ }
            } else {
                if (badCount != -1) {
                    Log.d("GraficLog", "Total registros malos: $badCount")
                    Log.d("GraficLog", "Total de elementos: ${recordList.size}")

                    btViewModel.updateBtState { currentState ->
                        currentState.copy(
                            graph = recordList,
                            recordQuery = QueryState.RECORD_RECEIVED
                        )
                    }
                }

                badCount = -1;
                recordList = mutableListOf()
            }
        } else if (parts.size == 1 && parts[0] == "config_ok") {
            if (btViewModel.getConfigState() == QueryState.CONFIG_SENT) {
                btViewModel.updateBtState { it.copy(configQuery = QueryState.CONFIG_OK) }
                btViewModel.getDeviceConfig()
            }
        } else if (parts.size == 1 && parts[0] == "config_error") {
            if (btViewModel.getConfigState() == QueryState.CONFIG_ASKED) {
                btViewModel.updateBtState { it.copy(configQuery = QueryState.ERROR) }
            }
        } else if (parts.size == 1 && parts[0] == "thresholds_ok") {
            if (btViewModel.getThresholdsState() == QueryState.THRESHOLDS_SENT) {
                btViewModel.updateBtState { it.copy(thresholdsQuery = QueryState.THRESHOLDS_OK) }
                btViewModel.getDeviceThresholds()
            }
        } else if (parts.size == 1 && parts[0] == "thresholds_error") {
            if (btViewModel.getThresholdsState() == QueryState.THRESHOLDS_ASKED) {
                btViewModel.updateBtState { it.copy(thresholdsQuery = QueryState.ERROR) }
            }
        } else if (parts.size == 1 && parts[0] == "record_erased") {
            if (btViewModel.getRecordErasedState() == QueryState.ERASING) {
                btViewModel.updateBtState { it.copy(recordQuery = QueryState.ERASED) }
            }
        } else if (parts.size == 1 && parts[0] == "wifi_error") {
            if (btViewModel.getWifiState() == ConnectionState.CONNECTING) {
                btViewModel.updateBtState { it.copy(wifiQuery = ConnectionState.ERROR) }
            }
        } else if (parts.size == 1 && parts[0] == "wifi_error_pass") {
            if (btViewModel.getWifiState() == ConnectionState.CONNECTING) {
                btViewModel.updateBtState { it.copy(wifiQuery = ConnectionState.ERROR_PASS) }
            }
        } else if (parts.size == 1 && parts[0] == "wifi_ok") {
            if (btViewModel.getWifiState() in listOf(ConnectionState.CONNECTING, ConnectionState.OFFLINE)) {
                btViewModel.updateBtState { it.copy(wifiQuery = ConnectionState.CONNECTED) }
            }
        } else {
            Log.w(TAG, "Mensaje con formato incorrecto: $message")
        }
    }

    private fun monitorDisconnection(socket: BluetoothSocket) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val buffer = ByteArray(1)
                while (true) {
                    val read = withContext(Dispatchers.IO) {
                        try {
                            socket.inputStream.read(buffer)
                        } catch (e: IOException) {
                            -1 // Indica que la conexión se cerró o hubo un error
                        }
                    }
                    if (read == -1) break
                    // No necesitamos procesar el byte, solo verificar si se puede leer
                }
            } finally {
                Log.i("BluetoothHelper", "Desconexión detectada.")
                try {
                    socket.close()
                } catch (_: IOException) {}
                _connectedSocket.value = null
            }
        }
    }

    private fun hasPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun getFilteredPairedDevices(activity: Activity): List<BluetoothDevice> {
        if (!hasPermission(activity)) return emptyList()
        return bluetoothAdapter?.bondedDevices
            ?.filter { it.name?.startsWith("Refrimed", ignoreCase = true) == true }
            ?.toList() ?: emptyList()
    }

    fun closeConnection() {
        val currentSocket = _connectedSocket.value
        if (currentSocket != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    currentSocket.close()
                    _connectedSocket.value = null
                    Log.i("BluetoothHelper", "Conexión cerrada manualmente.")
                } catch (e: IOException) {
                    Log.e("BluetoothHelper", "Error al cerrar la conexión: ${e.message}")
                }
            }
        } else {
            Log.w("BluetoothHelper", "No hay ninguna conexión activa para cerrar.")
        }
    }
}