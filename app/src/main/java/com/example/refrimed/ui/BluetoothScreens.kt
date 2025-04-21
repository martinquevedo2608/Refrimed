package com.example.refrimed.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.refrimed.data.AlarmData
import com.example.refrimed.data.BtUiState
import com.example.refrimed.data.ConnectionState
import com.example.refrimed.data.QueryState
import com.example.refrimed.data.Screen
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun BluetoothScreen(
    refrimedViewModel: RefrimedViewModel,
    btViewModel: BluetoothViewModel = viewModel(factory = AppViewModelProvider.Factory),
    btUiState: BtUiState,
    connectedSocket: BluetoothSocket?,
    deviceConfig: String
) {

    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val refrimedDevices = remember { btViewModel.getPairedDevices(activity) }

    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }

    // NUEVO: Estado local para los mensajes individuales
    val messageList = remember { mutableStateListOf<String>() }

    // Recolecta mensajes en tiempo real desde messagesFlow
    LaunchedEffect(Unit) {
        btViewModel.messagesFlow.collect { message ->
            messageList.add(0, message) // Agrega al inicio para ver el más reciente arriba
            if (messageList.size > 100) {
                messageList.removeLast() // Evita crecimiento infinito
            }
            // La lógica de parseo YA NO ESTÁ AQUÍ
        }
    }

    Text(
        text = if (connectedSocket != null) {
            "Conectado a ${getDeviceNameSafe(connectedSocket.remoteDevice, context)}"
        } else if (btUiState.connectionState == ConnectionState.CONNECTING) {
            "Conectando..."
        } else {
            "Sin conexión"
        },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(8.dp)
    )

    if (connectedSocket != null) btViewModel.setConnectionState(ConnectionState.ONLINE)

    //var btConnectionErrorFlag = remember { mutableStateOf(false) }
    if (btUiState.connectionState == ConnectionState.ERROR) Toast.makeText(
        context,
        "Error en la conexión bluetooth",
        Toast.LENGTH_SHORT
    ).show()

    DeviceDropdown(
        devices = refrimedDevices,
        selectedDevice = selectedDevice,
        onDeviceSelected = { selectedDevice = it },
        deviceConfig = deviceConfig
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                btViewModel.setConnectionState(ConnectionState.CONNECTING)
                selectedDevice?.let {
                    btViewModel.connectTo(context, it)
                }
            },
            enabled = (selectedDevice != null && connectedSocket == null),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "Conectar",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
        }

        Button(
            onClick = {
                btViewModel.disconnect()
            },
            enabled = connectedSocket != null,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "Desconectar",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

    if (connectedSocket != null) {
        Text(
            text = "Mediciones actuales:",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                textDecoration = TextDecoration.Underline,
                fontSize = 20.sp
            ),
            modifier = Modifier.padding(12.dp)
        )

        Text(
            buildAnnotatedString {
                append("Hora: ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    if (btUiState.actualDateTime == Date(1)) {
                        append("---")
                    } else if (btUiState.actualDateTime == Date(0)) {
                        append("Sin conexión WiFi")
                    } else {
                        val horaActual = SimpleDateFormat("HH:mm:ss").format(btUiState.actualDateTime)
                        append("$horaActual hs")
                    }
                }
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(4.dp)
        )

        if (btUiState.temperatures != "Off") {
            Text(
                buildAnnotatedString {
                    append("Temperatura 1: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(if (btUiState.actualTemperature1 < -100) "Desconectado" else "${btUiState.actualTemperature1} °C")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(4.dp)
            )
        }

        if (btUiState.temperatures !in listOf("Off", "1")) {
            Text(
                buildAnnotatedString {
                    append("Temperatura 2: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(if (btUiState.actualTemperature2 < -100) "Desconectado" else "${btUiState.actualTemperature2} °C")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(4.dp)
            )
        }

        if (btUiState.temperatures !in listOf("Off", "1", "2")) {
            Text(
                buildAnnotatedString {
                    append("Temperatura 3: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(if (btUiState.actualTemperature3 < -100) "Desconectado" else "${btUiState.actualTemperature3} °C")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(4.dp)
            )
        }

        if (btUiState.temperatures !in listOf("Off", "1", "2", "3")) {
            Text(
                buildAnnotatedString {
                    append("Temperatura 4: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(if (btUiState.actualTemperature4 < -100) "Desconectado" else "${btUiState.actualTemperature4} °C")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(4.dp)
            )
        }

        if (btUiState.currents != "Off") {
            Text(
                buildAnnotatedString {
                    append("Corriente: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${btUiState.actualCurrent} A")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(4.dp)
            )
        }

        if (btUiState.relays != "Off") {
            Text(
                buildAnnotatedString {
                    append("Relé 1: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(if (btUiState.actualRelay1) "On" else "Off")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(4.dp)
            )
        }

        if (btUiState.relays !in listOf("Off", "1")) {
            Text(
                buildAnnotatedString {
                    append("Relé 2: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(if (btUiState.actualRelay2) "On" else "Off")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(4.dp)
            )
        }

        if (btUiState.alarms != "Off") {
            Text(
                buildAnnotatedString {
                    append("Alarma 1: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(if (btUiState.actualAlarm1) "On" else "Off")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(4.dp)
            )
        }

        if (btUiState.alarms !in listOf("Off", "1")) {
            Text(
                buildAnnotatedString {
                    append("Alarma 2: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(if (btUiState.actualAlarm2) "On" else "Off")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(4.dp)
            )
        }

        if (btUiState.alarms !in listOf("Off", "1", "2")) {
            Text(
                buildAnnotatedString {
                    append("Alarma 3: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(if (btUiState.actualAlarm3) "On" else "Off")
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    refrimedViewModel.setScreen(Screen.BLUETOOTH_DEVICE_CONFIG)
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .weight(1f)
            ) {
                Text("Configurar dispositivo",
                    style = TextStyle(fontWeight = FontWeight.Bold)
                )
            }

            Button(
                onClick = {
                    refrimedViewModel.setScreen(Screen.BLUETOOTH_THRESHOLDS_CONFIG)
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .weight(1f)
            ) {
                Text("Configurar relés y alarmas",
                    style = TextStyle(fontWeight = FontWeight.Bold)
                )
            }
        }

        Button(
            onClick = {
                refrimedViewModel.setScreen(Screen.BLUETOOTH_GRAFICO)
            },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("Gráfico",
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
        }

    }

    if (btUiState.configQuery == QueryState.ERROR){
        Toast.makeText(LocalContext.current, "Error al obtener la configuración del dispositivo. Reconecte", Toast.LENGTH_LONG).show()
        btViewModel.disconnect()
        btViewModel.resetConfigStates()
    }
    if(btUiState.configQuery == QueryState.CONFIG_RECEIVED) {
        btViewModel.resetConfigStates()
    }
    if (btUiState.thresholdsQuery == QueryState.THRESHOLDS_RECEIVED) {
        btViewModel.resetThresholdsStates()
    }
}

@Composable
fun BluetoothConfigScreen(
    btViewModel: BluetoothViewModel,
    btUiState: BtUiState,
) {
    val config = remember { mutableStateListOf(btUiState.red, btUiState.pass, btUiState.temperatures, btUiState.currents, btUiState.relays, btUiState.alarms, btUiState.frecuenciaRegistro) }

    key (btUiState.configQuery) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Parámetros básicos:",
                modifier = Modifier.padding(8.dp)
            )

            OutlinedTextField(
                value = config[0],
                onValueChange = { config[0] = it },
                label = { Text("Red Wifi") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = config[1],
                onValueChange = { config[1] = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            DropdownSelector(
                label = "Temperaturas",
                options = listOf("Off", "1", "2", "3", "4"),
                onOptionSelected = { config[2] = it },
                startValue = config[2],
                modifier = Modifier.padding(vertical = 8.dp)
            )

            DropdownSelector(
                label = "Corriente",
                options = listOf("Off", "On"),
                onOptionSelected = { config[3] = it },
                startValue = config[3],
                modifier = Modifier.padding(vertical = 8.dp)
            )

            DropdownSelector(
                label = "Relés",
                options = listOf("Off", "1", "2"),
                onOptionSelected = { config[4] = it },
                startValue = config[4],
                modifier = Modifier.padding(vertical = 8.dp)
            )

            DropdownSelector(
                label = "Alarmas",
                options = listOf("Off", "1", "2", "3"),
                onOptionSelected = { config[5] = it },
                startValue = config[5],
                modifier = Modifier.padding(vertical = 8.dp)
            )

            DropdownSelector(
                label = "Frecuencia de registro",
                options = listOf(
                    "15 segundos",
                    "30 segundos",
                    "1 minuto",
                    "3 minutos",
                    "5 minutos",
                    "10 minutos",
                    "30 minutos",
                    "1 hora"
                ),
                onOptionSelected = { config[6] = it },
                startValue = config[6],
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        btViewModel.getDeviceConfig()
                    },
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text("Leer config. actual")
                }

                Button(
                    onClick = {
                        btViewModel.sendMessage("set_red:${config[0]}")
                        btViewModel.sendMessage("set_pass:${config[1]}")
                        btViewModel.sendMessage("set_temperaturas:${config[2]}")
                        btViewModel.sendMessage("set_corrientes:${config[3]}")
                        btViewModel.sendMessage("set_reles:${config[4]}")
                        btViewModel.sendMessage("set_alarmas:${config[5]}")
                        btViewModel.sendMessage("set_frecuencia:${config[6]}")
                        btViewModel.setConfigSent()
                    },
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        "Guardar configuración",
                        style = TextStyle(fontWeight = FontWeight.Bold)
                    )
                }
            }

            HorizontalDivider(color = Color.Gray, thickness = 4.dp)

            Text(
                "Conectividad WiFi:",
                modifier = Modifier.padding(8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        btViewModel.wifiConnect()
                    },
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    if (btUiState.wifiQuery == ConnectionState.CONNECTING) {
                        CircularProgressIndicator()
                    } else {
                        Text(
                            "Conectar",
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
                var wifiConnectionState by remember { mutableStateOf("") }
                if (btUiState.wifiQuery == ConnectionState.OFFLINE) wifiConnectionState =
                    "--> (Ahora Desconectado)"
                if (btUiState.wifiQuery == ConnectionState.ONLINE) wifiConnectionState =
                    "--> (Ahora Conectado!)"
                Text(wifiConnectionState, style = TextStyle(fontStyle = FontStyle.Italic))
            }

            HorizontalDivider(color = Color.Gray, thickness = 4.dp)

            Text(
                "Registro y gráfico:",
                modifier = Modifier.padding(8.dp)
            )

            BotonBorrado(btViewModel)

            if (btUiState.configQuery == QueryState.ERROR) {
                Toast.makeText(
                    LocalContext.current,
                    "ERROR en el envío de configuración. Intente nuevamente.",
                    Toast.LENGTH_LONG
                ).show()
                btViewModel.resetConfigStates()
            }
            if (btUiState.configQuery == QueryState.CONFIG_RECEIVED) {
                if (btUiState.configSent) {
                    Toast.makeText(
                        LocalContext.current,
                        "Configuración GUARDADA exitosamente.",
                        Toast.LENGTH_LONG
                    ).show()
                    btViewModel.resetConfigStates()
                } else {
                    Toast.makeText(
                        LocalContext.current,
                        "Configuración LEIDA exitosamente del dispositivo.",
                        Toast.LENGTH_LONG
                    ).show()
                    btViewModel.resetConfigStates()
                }
            }
            if (btUiState.recordQuery == QueryState.ERASED) {
                Toast.makeText(
                    LocalContext.current,
                    "Registro interno correctamente BORRADO.",
                    Toast.LENGTH_LONG
                ).show()
                btViewModel.resetRecordStates()
            }

            if (btUiState.wifiQuery == ConnectionState.ERROR) {
                Toast.makeText(
                    LocalContext.current,
                    "ERROR al realizar la conexión WiFi",
                    Toast.LENGTH_LONG
                ).show()
                btViewModel.setWifiState(ConnectionState.OFFLINE)
            }

            if (btUiState.wifiQuery == ConnectionState.ERROR_PASS) {
                Toast.makeText(
                    LocalContext.current,
                    "ERROR al realizar la conexión WiFi. Verifique Usuario y Contraseña.",
                    Toast.LENGTH_LONG
                ).show()
                btViewModel.setWifiState(ConnectionState.OFFLINE)
            }

            if (btUiState.wifiQuery == ConnectionState.CONNECTED) {
                Toast.makeText(LocalContext.current, "WiFi conexión exitosa.", Toast.LENGTH_LONG)
                    .show()
                btViewModel.setWifiState(ConnectionState.ONLINE)
            }
        }
    }
}

@Composable
fun BluetoothThresholdsScreen(
    btViewModel: BluetoothViewModel,
    btUiState: BtUiState
) {
    val magnitud = when (btUiState.temperatures) {
        "Off" -> listOf("Off")
        "1" -> listOf("Temperatura 1", "Off")
        "2" -> listOf("Temperatura 1", "Temperatura 2", "Off")
        "3" -> listOf("Temperatura 1", "Temperatura 2", "Temperatura 3", "Off")
        else -> listOf("Temperatura 1", "Temperatura 2", "Temperatura 3", "Temperatura 4", "Off")
    }.let { baseList ->
        if (btUiState.currents == "On") listOf("Corriente") + baseList else baseList
    }

    val alarmThresholds = remember { mutableStateListOf(AlarmData(), AlarmData(), AlarmData()) }
    val relayThresholdsState = listOf(btUiState.thresholdsRelay1, btUiState.thresholdsRelay2)
    val relayThresholds = remember { mutableStateListOf("", "") }


    key(btUiState.thresholdsAlarm1_mag) {
        if (btUiState.alarms in listOf("1", "2", "3")) {
            AlarmItem(
                i = 1,
                magnitud = magnitud,
                startMag = btUiState.thresholdsAlarm1_mag,
                startOp = btUiState.thresholdsAlarm1_op,
                startVal = btUiState.thresholdsAlarm1_num,
                onAlarmaChange = { temp, op, num ->
                    alarmThresholds[0] = AlarmData(temp, op, num.toString())
                }
            )
        }

        if (btUiState.alarms in listOf("2", "3")) {
            AlarmItem(
                i = 2,
                magnitud = magnitud,
                startMag = btUiState.thresholdsAlarm2_mag,
                startOp = btUiState.thresholdsAlarm2_op,
                startVal = btUiState.thresholdsAlarm2_num,
                onAlarmaChange = { temp, op, num ->
                    alarmThresholds[1] = AlarmData(temp, op, num.toString())
                }
            )
        }

        if (btUiState.alarms == "3") {
            AlarmItem(
                i = 3,
                magnitud = magnitud,
                startMag = btUiState.thresholdsAlarm3_mag,
                startOp = btUiState.thresholdsAlarm3_op,
                startVal = btUiState.thresholdsAlarm3_num,
                onAlarmaChange = { temp, op, num ->
                    alarmThresholds[2] = AlarmData(temp, op, num.toString())
                }
            )
        }


        val releOptionList = when (btUiState.alarms) {
            "Off" -> listOf("Start test (500ms)", "Start test (750ms)", "Start test (1s)")
            "1" -> listOf("Alarma 1", "Run test (500ms)", "Run test (750ms)", "Run test (1s)")
            "2" -> listOf(
                "Alarma 1",
                "Alarma 2",
                "Run test (500ms)",
                "Run test (750ms)",
                "Run test (1s)"
            )

            "3" -> listOf(
                "Alarma 1",
                "Alarma 2",
                "Alarma 3",
                "Run test (500ms)",
                "Run test (750ms)",
                "Run test (1s)"
            )

            else -> {
                emptyList()
            }
        }



        if (btUiState.relays in listOf("1", "2")) {
            for (i in 1..btUiState.relays.toInt()) {
                DropdownSelector(
                    label = "Relé $i",
                    options = releOptionList,
                    onOptionSelected = { relayThresholds[i - 1] = it },
                    startValue = relayThresholdsState[i - 1],
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    btViewModel.getDeviceThresholds()
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.secondary)
            ) {
                Text("Leer config. actual")
            }

            Button(
                onClick = {
                    btViewModel.sendMessage("set_alarm1:${alarmThresholds[0].mag},${alarmThresholds[0].op},${alarmThresholds[0].num}")
                    btViewModel.sendMessage("set_alarm2:${alarmThresholds[1].mag},${alarmThresholds[1].op},${alarmThresholds[1].num}")
                    btViewModel.sendMessage("set_alarm3:${alarmThresholds[2].mag},${alarmThresholds[2].op},${alarmThresholds[2].num}")
                    btViewModel.sendMessage("set_rele1:${relayThresholds[0]}")
                    btViewModel.sendMessage("set_rele2:${relayThresholds[1]}")
                    btViewModel.setThresholdsSent()
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text("Guardar configuración")
            }
        }

        if (btUiState.thresholdsQuery == QueryState.ERROR) {
            Toast.makeText(LocalContext.current, "ERROR en el envío de configuración. Intente nuevamente.", Toast.LENGTH_LONG).show()
            btViewModel.resetThresholdsStates()
        }
        if (btUiState.thresholdsQuery == QueryState.THRESHOLDS_RECEIVED) {
            if(btUiState.thresholdsSent) {
                Toast.makeText(LocalContext.current, "Configuración GUARDADA exitosamente.", Toast.LENGTH_LONG).show()
                btViewModel.resetThresholdsStates()
            } else {
                Toast.makeText(LocalContext.current, "Configuración LEIDA exitosamente del dispositivo.", Toast.LENGTH_LONG).show()
                btViewModel.resetThresholdsStates()
            }
        }
    }

}

@Composable
fun GraficoScreen(
    btViewModel: BluetoothViewModel,
    btUiState: BtUiState
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = {
                btViewModel.getRecord()
            },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.tertiary)
        ) {
            Text("Leer reg. completo")
        }

        Button(
            onClick = {
                btViewModel.getRecord(2*60*3)
            },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.tertiary)
        ) {
            Text("Leer últimas 3hs")
        }

    }

    key(btUiState.graph) {
        if (btUiState.graph.size > 1 && btUiState.recordQuery in listOf(QueryState.IDLE, QueryState.RECORD_RECEIVED)) {

            val modelProducer = remember { CartesianChartModelProducer() }
            val temp1Graph = remember(btUiState.graph) {
                if (btUiState.temperatures in listOf("1", "2", "3", "4")) {
                    btUiState.graph.map { it.timestamp to it.temp1 }
                } else emptyList()
            }
            val temp2Graph = remember(btUiState.graph) {
                if (btUiState.temperatures in listOf("2", "3", "4")) {
                    btUiState.graph.map { it.timestamp to it.temp2 }
                } else emptyList()
            }
            val temp3Graph = remember(btUiState.graph) {
                if (btUiState.temperatures in listOf("3", "4")) {
                    btUiState.graph.map { it.timestamp to it.temp3 }
                } else emptyList()
            }
            val temp4Graph = remember(btUiState.graph) {
                if (btUiState.temperatures == "4") {
                    btUiState.graph.map { it.timestamp to it.temp4 }
                } else emptyList()
            }
            val currentGraph = remember(btUiState.graph) {
                if (btUiState.currents == "On") {
                    btUiState.graph.map { it.timestamp to it.current * 10 }
                } else emptyList()
            }
            val relay1Graph = remember(btUiState.graph) {
                if (btUiState.relays in listOf("1", "2")) {
                    btUiState.graph.map { it.timestamp to if(it.relay1) 1 else 0  }
                } else emptyList()
            }
            val relay2Graph = remember(btUiState.graph) {
                if (btUiState.relays == "2") {
                    btUiState.graph.map { it.timestamp to if(it.relay2) 3 else 2  }
                } else emptyList()
            }
            val alarm1Graph = remember(btUiState.graph) {
                if (btUiState.alarms in listOf("1", "2", "3")) {
                    btUiState.graph.map { it.timestamp to if(it.alarm1) 5 else 4  }
                } else emptyList()
            }
            val alarm2Graph = remember(btUiState.graph) {
                if (btUiState.alarms in listOf("2", "3")) {
                    btUiState.graph.map { it.timestamp to if(it.alarm2) 7 else 6  }
                } else emptyList()
            }
            val alarm3Graph = remember(btUiState.graph) {
                if (btUiState.alarms == "3") {
                    btUiState.graph.map { it.timestamp to if(it.alarm3) 9 else 8  }
                } else emptyList()
            }

            val zoomState = rememberVicoZoomState()//initialZoom = Zoom.Content)
            val scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End)

            LaunchedEffect(temp1Graph, temp2Graph, currentGraph) {
                modelProducer.runTransaction {
                    lineSeries {
                        if (temp1Graph.isNotEmpty()) {
                            series(
                                x = temp1Graph.map { it.first.time.toFloat() },
                                y = temp1Graph.map { it.second }
                            )
                        }
                        if (temp2Graph.isNotEmpty()) {
                            series(
                                x = temp2Graph.map { it.first.time.toFloat() },
                                y = temp2Graph.map { it.second }
                            )
                        }
                        if (temp3Graph.isNotEmpty()) {
                            series(
                                x = temp3Graph.map { it.first.time.toFloat() },
                                y = temp3Graph.map { it.second }
                            )
                        }
                        if (temp4Graph.isNotEmpty()) {
                            series(
                                x = temp4Graph.map { it.first.time.toFloat() },
                                y = temp4Graph.map { it.second }
                            )
                        }
                        if (currentGraph.isNotEmpty()) {
                            series(
                                x = currentGraph.map { it.first.time.toFloat() },
                                y = currentGraph.map { it.second }
                            )
                        }
                        if (relay1Graph.isNotEmpty()) {
                            series(
                                x = relay1Graph.map { it.first.time.toFloat() },
                                y = relay1Graph.map { it.second }
                            )
                        }
                        if (relay2Graph.isNotEmpty()) {
                            series(
                                x = relay2Graph.map { it.first.time.toFloat() },
                                y = relay2Graph.map { it.second }
                            )
                        }
                        if (alarm1Graph.isNotEmpty()) {
                            series(
                                x = alarm1Graph.map { it.first.time.toFloat() },
                                y = alarm1Graph.map { it.second }
                            )
                        }
                        if (alarm2Graph.isNotEmpty()) {
                            series(
                                x = alarm2Graph.map { it.first.time.toFloat() },
                                y = alarm2Graph.map { it.second }
                            )
                        }
                        if (alarm3Graph.isNotEmpty()) {
                            series(
                                x = alarm3Graph.map { it.first.time.toFloat() },
                                y = alarm3Graph.map { it.second }
                            )
                        }
                    }
                }
            }

            val dateFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

            val lineColors = mutableListOf<LineCartesianLayer.Line>()
            val legendItems = mutableListOf<LegendItem>()

            if (temp1Graph.isNotEmpty()) {
                lineColors.add(LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill(Color(0xFF6495ED).toArgb()))))
                legendItems.add(LegendItem(
                    icon = LineComponent(fill = Fill(Color(0xFF6495ED).toArgb())),
                    labelComponent = TextComponent(color = MaterialTheme.colorScheme.tertiary.toArgb(), textSizeSp = 16f),
                    label = btUiState.legendNames[0]
                ))
            }
            if (temp2Graph.isNotEmpty()) {
                lineColors.add(LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill(Color(0xFF0000FF).toArgb()))))
                legendItems.add(LegendItem(
                    icon = LineComponent(fill = Fill(Color(0xFF0000FF).toArgb())),
                    labelComponent = TextComponent(color = MaterialTheme.colorScheme.tertiary.toArgb(), textSizeSp = 16f),
                    label = btUiState.legendNames[1]
                ))
            }
            if (temp3Graph.isNotEmpty()) {
                lineColors.add(LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill(Color(0xFF00008B).toArgb()))))
                legendItems.add(LegendItem(
                    icon = LineComponent(fill = Fill(Color(0xFF00008B).toArgb())),
                    labelComponent = TextComponent(color = MaterialTheme.colorScheme.tertiary.toArgb(), textSizeSp = 16f),
                    label = btUiState.legendNames[2]
                ))
            }
            if (temp4Graph.isNotEmpty()) {
                lineColors.add(LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill(Color(0xFFFFC0CB).toArgb()))))
                legendItems.add(LegendItem(
                    icon = LineComponent(fill = Fill(Color(0xFFFFC0CB).toArgb())),
                    labelComponent = TextComponent(color = MaterialTheme.colorScheme.tertiary.toArgb(), textSizeSp = 16f),
                    label = btUiState.legendNames[3]
                ))
            }
            if (currentGraph.isNotEmpty()) {
                lineColors.add(LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill(Color(0xFFFF0000).toArgb()))))
                legendItems.add(LegendItem(
                    icon = LineComponent(fill = Fill(Color(0xFFFF0000).toArgb())),
                    labelComponent = TextComponent(color = MaterialTheme.colorScheme.tertiary.toArgb(), textSizeSp = 16f),
                    label = btUiState.legendNames[4]
                ))
            }
            if (relay1Graph.isNotEmpty()) {
                lineColors.add(LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill(Color(0xFF808080).toArgb()))))
                legendItems.add(LegendItem(
                    icon = LineComponent(fill = Fill(Color(0xFF808080).toArgb())),
                    labelComponent = TextComponent(color = MaterialTheme.colorScheme.tertiary.toArgb(), textSizeSp = 16f),
                    label = btUiState.legendNames[5]
                ))
            }
            if (relay2Graph.isNotEmpty()) {
                lineColors.add(LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill(Color(0xFF808080).toArgb()))))
                legendItems.add(LegendItem(
                    icon = LineComponent(fill = Fill(Color(0xFF808080).toArgb())),
                    labelComponent = TextComponent(color = MaterialTheme.colorScheme.tertiary.toArgb(), textSizeSp = 16f),
                    label = btUiState.legendNames[6]
                ))
            }
            if (alarm1Graph.isNotEmpty()) {
                lineColors.add(LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill(Color(0xFFFFFF00).toArgb()))))
                legendItems.add(LegendItem(
                    icon = LineComponent(fill = Fill(Color(0xFFFFFF00).toArgb())),
                    labelComponent = TextComponent(color = MaterialTheme.colorScheme.tertiary.toArgb(), textSizeSp = 16f),
                    label = btUiState.legendNames[7]
                ))
            }
            if (alarm2Graph.isNotEmpty()) {
                lineColors.add(LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill(Color(0xFF90EE90).toArgb()))))
                legendItems.add(LegendItem(
                    icon = LineComponent(fill = Fill(Color(0xFF90EE90).toArgb())),
                    labelComponent = TextComponent(color = MaterialTheme.colorScheme.tertiary.toArgb(), textSizeSp = 16f),
                    label = btUiState.legendNames[8]
                ))
            }
            if (alarm3Graph.isNotEmpty()) {
                lineColors.add(LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill(Color(0xFFFFA500).toArgb()))))
                legendItems.add(LegendItem(
                    icon = LineComponent(fill = Fill(Color(0xFFFFA500).toArgb())),
                    labelComponent = TextComponent(color = MaterialTheme.colorScheme.tertiary.toArgb(), textSizeSp = 16f),
                    label = btUiState.legendNames[9]
                ))
            }

            CartesianChartHost(
                rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(lineColors)
                    ),
                    legend = rememberHorizontalLegend(items = {
                        for (legendItem in legendItems) {
                            add(legendItem)
                        }
                    },),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = { context: CartesianMeasuringContext, value: Double, position: Axis.Position.Vertical? ->
                            try {
                                val date = Date(value.toLong()) // Convert Double to Long for Date
                                dateFormatter.format(date)
                            } catch (e: NumberFormatException) {
                                Log.e("TemperatureChart", "Error formatting X-axis label: $value", e)
                                ""
                            }
                        },
                        labelRotationDegrees = 90f
                    ),
                ),
                modelProducer,
                scrollState = scrollState,
                zoomState = zoomState,
                modifier = Modifier.fillMaxSize()
            )

            btViewModel.setGraphState(QueryState.IDLE)


        } else if (btUiState.recordQuery == QueryState.LOADING) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, // Centra horizontalmente los elementos
                verticalArrangement = Arrangement.Center, // Centra verticalmente los elementos
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Leyendo registros...")
            }
        }
    }
}




@Composable
fun AlarmItem(
    i: Int = 0,
    magnitud: List<String>,
    startMag: String,
    operadores: List<String> = listOf("<", ">"),
    startOp: String,
    startVal: String,
    onAlarmaChange: (String, String, Double) -> Unit // (temperatura, operador, valor)
) {
    var selectedMag by remember { mutableStateOf(startMag) }
    var selectedOperador by remember { mutableStateOf(startOp) }
    var inputValue by remember { mutableStateOf(startVal) }
    var unidad by remember { mutableStateOf("") }

    fun callOnChange() {
        val valor = inputValue.toDoubleOrNull() ?: 0.0
        onAlarmaChange(selectedMag, selectedOperador, valor)
        unidad = if (selectedMag == "Corriente") "A" else "°C"
    }

    LaunchedEffect(Unit) {
        callOnChange()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DropdownSelector(
            options = magnitud,
            label = "Alarma $i, asociada a:",
            startValue = startMag,
            onOptionSelected = {
                selectedMag = it
                callOnChange()
            },
            //modifier = Modifier.weight(1f)
        )

        // Operadores
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            operadores.forEach { op ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedOperador == op,
                        onClick = {
                            selectedOperador = op
                            callOnChange()
                        }
                    )
                    Text(op)
                }
            }

            Spacer(modifier = Modifier.width(48.dp))

            // Input de valor Double
            OutlinedTextField(
                value = inputValue,
                onValueChange = {
                    inputValue = it
                    callOnChange()
                },
                label = { Text("Valor $i") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(100.dp)
            )

            // Unidad (A / °C)
            Text(unidad)
        }
    }
    Spacer(modifier = Modifier.height(16.dp))

}



@Composable
fun RequestBluetoothPermissions() {
    val context = LocalContext.current
    val permissions = listOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val granted = permissionsMap.all { it.value }
        if (!granted) {
            Toast.makeText(context, "Se requieren permisos de Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (permissions.any {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }
        ) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}

@SuppressLint("MissingPermission")
fun getDeviceNameSafe(device: BluetoothDevice, context: Context): String {
    return if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        device.name ?: "Sin nombre"
    } else {
        "Dispositivo"
    }
}

@Composable
fun BotonBorrado(btViewModel: BluetoothViewModel) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar borrado") },
            text = {
                Text("Estás por borrar el registro interno completo. Esta acción no se puede deshacer. ¿Deseás continuar?")
            },
            confirmButton = {
                TextButton(onClick = {
                    btViewModel.getRecordErased()
                    showDialog = false
                }) {
                    Text("Sí, borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Button(
        onClick = {
            showDialog = true
        },
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text("Borrar registro interno")
    }
}