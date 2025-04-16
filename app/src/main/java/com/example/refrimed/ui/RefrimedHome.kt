package com.example.refrimed.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.refrimed.R
import com.example.refrimed.data.Screen
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.refrimed.data.BtUiState
import com.example.refrimed.data.ConnectionState
import com.example.refrimed.data.QueryState
import java.text.SimpleDateFormat
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawStyle
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.LineType
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import com.example.refrimed.data.AlarmData
import com.example.refrimed.data.Values
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun RefrimedHome(
    contentPadding: PaddingValues
) {
    val context = LocalContext.current

    val refrimedViewModel: RefrimedViewModel = viewModel(
        factory = AppViewModelProvider.Factory,
        key = "refrimedViewModel"
    )

    val btViewModel: BluetoothViewModel = viewModel(
        factory = AppViewModelProvider.Factory,
        key = "bluetoothViewModel"
    )

    val refrimedUiState by refrimedViewModel.uiState.collectAsState()
    val btUiState by btViewModel.btState.collectAsState()
    val connectedSocket by btViewModel.connectedSocket.collectAsState()

    if (connectedSocket == null) {
        btViewModel.resetConfigStates()
        btViewModel.resetThresholdsStates()
        btViewModel.resetRecordStates()
    }

    BackHandler {
        when (refrimedUiState.screen) {
            Screen.BLUETOOTH, Screen.CLOUD, Screen.NOTES -> refrimedViewModel.setScreen(Screen.HOME)
            Screen.HOME -> {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            Screen.BLUETOOTH_THRESHOLDS_CONFIG, Screen.BLUETOOTH_DEVICE_CONFIG, Screen.BLUETOOTH_GRAFICO -> refrimedViewModel.setScreen(Screen.BLUETOOTH)
            else -> { }
        }
    }


    Column (
        modifier = Modifier
            .padding(contentPadding)
            .padding(16.dp)
            //.verticalScroll(rememberScrollState())
    ) {
        if (refrimedUiState.screen == Screen.HOME) {
            CardItem(
                "Conexión bluetooth",
                R.drawable.refrimed_bluetooth,
                "Las configuraciones en el equipo solo se hacen mediante este método de conexión",
                onClick = { refrimedViewModel.setScreen(Screen.BLUETOOTH) }
            )
            CardItem(
                "Conexión remota",
                R.drawable.refrimed_cloud,
                "Acceso a mediciones y registros desde la base de datos de Betta Ingeniería",
                onClick = { refrimedViewModel.setScreen(Screen.CLOUD) }
            )
            CardItem(
                "Notas útiles",
                R.drawable.refrimed_notes,
                "Apartado con utilidades y notas técnicas",
                onClick = { refrimedViewModel.setScreen(Screen.NOTES) }
            )
        }

        if (refrimedUiState.screen == Screen.BLUETOOTH) {
            BluetoothScreen(refrimedViewModel, btViewModel, btUiState, connectedSocket)
        }

        if (refrimedUiState.screen == Screen.BLUETOOTH_DEVICE_CONFIG) {
            BluetoothConfigScreen(btViewModel, btUiState, connectedSocket)
            if (connectedSocket == null) refrimedViewModel.setScreen(Screen.BLUETOOTH)
        }

        if (refrimedUiState.screen == Screen.BLUETOOTH_THRESHOLDS_CONFIG) {
            BluetoothThresholdsScreen(btViewModel, btUiState, connectedSocket)
            if (connectedSocket == null) refrimedViewModel.setScreen(Screen.BLUETOOTH)
        }

        if (refrimedUiState.screen == Screen.BLUETOOTH_GRAFICO) {
            GraficoScreen(btViewModel, btUiState)
            if (connectedSocket == null) refrimedViewModel.setScreen(Screen.BLUETOOTH)
        }
    }
}

@Composable
fun CardItem(
    title: String,
    image: Int,
    text: String,
    onClick: () -> Unit
) {
    Card (
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(32.dp),
        modifier = Modifier
            .padding(vertical = 32.dp)
            .clickable { onClick() }
    ) {
        val height = 180
        Row {
            Image(
                painter = painterResource(image),
                contentDescription = title,
                Modifier
                    .size(height.dp)
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.White)
            )

            Column (
                modifier = Modifier
                    .height(height.dp)
            ){
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(lineHeight = 42.sp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    modifier = Modifier
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.weight(0.5f))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .padding(8.dp)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun BluetoothScreen(
    refrimedViewModel: RefrimedViewModel,
    btViewModel: BluetoothViewModel = viewModel(factory = AppViewModelProvider.Factory),
    btUiState: BtUiState,
    connectedSocket: BluetoothSocket?
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
            "Conectado a ${getDeviceNameSafe(connectedSocket!!.remoteDevice, context)}"
        } else if (btUiState.connectionState == ConnectionState.CONNECTING) {
            "Conectando..."
        } else {
            "Sin conexión"
        },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(8.dp)
    )

    if (connectedSocket != null) btViewModel.setConnectionState(ConnectionState.CONNECTED)

    //var btConnectionErrorFlag = remember { mutableStateOf(false) }
    if (btUiState.connectionState == ConnectionState.ERROR) Toast.makeText(
        context,
        "Error en la conexión bluetooth",
        Toast.LENGTH_SHORT
    ).show()

    DeviceDropdown(
        devices = refrimedDevices,
        selectedDevice = selectedDevice,
        onDeviceSelected = { selectedDevice = it }
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = {
                btViewModel.setConnectionState(ConnectionState.CONNECTING)
                selectedDevice?.let {
                    btViewModel.connectTo(context, it)
                }
            },
            enabled = selectedDevice != null,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "Conectar",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
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
                    append("${SimpleDateFormat("HH:mm:ss").format(btUiState.actualDateTime)} hs")
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
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    refrimedViewModel.setScreen(Screen.BLUETOOTH_DEVICE_CONFIG)
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .weight(1f)
            ) {
                Text("Configurar dispositivo")
            }

            Button(
                onClick = {
                    refrimedViewModel.setScreen(Screen.BLUETOOTH_THRESHOLDS_CONFIG)
                    btViewModel.getDeviceThresholds()
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .weight(1f)
            ) {
                Text("Configurar relés y alarmas")
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
            Text("Gráfico")
        }

    }

    if (btUiState.configQuery == QueryState.ERROR){
        Toast.makeText(LocalContext.current, "Error al obtener la configuración del dispositivo. Reconecte", Toast.LENGTH_LONG).show()
        btViewModel.disconnect()
        btViewModel.resetConfigStates()
    }
    if(btUiState.configQuery == QueryState.SUCCESS) {
        btViewModel.resetConfigStates()
    }
}

@Composable
fun BluetoothConfigScreen(
    btViewModel: BluetoothViewModel,
    btUiState: BtUiState,
    connectedSocket: BluetoothSocket?
) {
    val config = remember { mutableStateListOf(btUiState.red, btUiState.pass, btUiState.temperatures, btUiState.currents, btUiState.relays, btUiState.alarms) }
    val context = LocalContext.current

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

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

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    btViewModel.getDeviceConfig()
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
                    btViewModel.sendMessage("set_red:${config[0]}")
                    btViewModel.sendMessage("set_pass:${config[1]}")
                    btViewModel.sendMessage("set_temperaturas:${config[2]}")
                    btViewModel.sendMessage("set_corrientes:${config[3]}")
                    btViewModel.sendMessage("set_reles:${config[4]}")
                    btViewModel.sendMessage("set_alarmas:${config[5]}")
                    btViewModel.setConfigSent()
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text("Guardar configuración")
            }
        }

        if(btUiState.configQuery == QueryState.ERROR) {
            Toast.makeText(LocalContext.current, "ERROR en el envío de configuración. Intente nuevamente.", Toast.LENGTH_LONG).show()
            btViewModel.resetConfigStates()
        }
        if(btUiState.configQuery == QueryState.SUCCESS) {
            if(btUiState.configSent) {
                Toast.makeText(LocalContext.current, "Configuración GUARDADA exitosamente.", Toast.LENGTH_LONG).show()
                btViewModel.resetConfigStates()
            } else {
                Toast.makeText(LocalContext.current, "Configuración LEIDA exitosamente del dispositivo.", Toast.LENGTH_LONG).show()
                btViewModel.resetConfigStates()
            }
        }
    }
}

@Composable
fun BluetoothThresholdsScreen(
    btViewModel: BluetoothViewModel,
    btUiState: BtUiState,
    connectedSocket: BluetoothSocket?
) {
    val context = LocalContext.current

    val magnitud = when (btUiState.temperatures) {
        "Off" -> listOf("Off")
        "1" -> listOf("Temperatura 1", "Off")
        "2" -> listOf("Temperatura 1", "Temperatura 2", "Off")
        "3" -> listOf("Temperatura 1", "Temperatura 2", "Temperatura 3", "Off")
        else -> listOf("Temperatura 1", "Temperatura 2", "Temperatura 3", "Temperatura 4", "Off")
    }.let { baseList ->
        if (btUiState.currents == "On") listOf("Corriente") + baseList else baseList
    }

    val alarmThresholdsState = listOf(
        btUiState.thresholdsAlarm1_mag, btUiState.thresholdsAlarm1_op, btUiState.thresholdsAlarm1_num,
        btUiState.thresholdsAlarm2_mag, btUiState.thresholdsAlarm2_op, btUiState.thresholdsAlarm2_num,
        btUiState.thresholdsAlarm3_mag, btUiState.thresholdsAlarm3_op, btUiState.thresholdsAlarm3_num
    )
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
            horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                    btViewModel.sendMessage("set_alarm1:${alarmThresholds[0].mag}, ${alarmThresholds[0].op}, ${alarmThresholds[0].num}")
                    btViewModel.sendMessage("set_alarm2:${alarmThresholds[1].mag}, ${alarmThresholds[1].op}, ${alarmThresholds[1].num}")
                    btViewModel.sendMessage("set_alarm3:${alarmThresholds[2].mag}, ${alarmThresholds[2].op}, ${alarmThresholds[2].num}")
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
        if (btUiState.thresholdsQuery == QueryState.SUCCESS) {
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
    Button(
        onClick = {
            btViewModel.getRecord()
        },
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.tertiary)
    ) {
        Text("Leer registro interno")
    }

    key(btUiState.graph) {
        if (btUiState.graph.size > 1 && btUiState.recordQuery == QueryState.IDLE) {
            Log.d("GraficLog", "Entrando en composable...")

            for (i in 100 downTo 1) {
                Log.d("GraficLog", "t1: ${btUiState.graph[btUiState.graph.size - i].temp1}, t2: ${btUiState.graph[btUiState.graph.size - i].temp2}")
            }

            val divisor: Int = btUiState.graph.size / 500
            Log.d("GraficLog", "Divisor: $divisor")
            val graphList = btUiState.graph.filterIndexed { index, _ -> index % divisor == 0 }


            // Para graficar la Temperatura 1 vs. Tiempo (usando timestamps en el eje X)
            val pointsDataTemp1Time = transformValuesToPointData(
                valuesList = graphList,
                yValueSelector = { it.temp1 },
                useIndexForX = false
            )

            // Para graficar la Temperatura 2 vs. Índice (orden de recepción)
            val pointsDataTemp2Index = transformValuesToPointData(
                valuesList = graphList,
                yValueSelector = { it.temp2 },
                useIndexForX = false
            )

            // Para graficar la Corriente vs. Tiempo (usando timestamps en el eje X)
            val pointsDataCurrentTime = transformValuesToPointData(
                valuesList = graphList,
                yValueSelector = { it.current },
                useIndexForX = false
            )

            val dateFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val xAxisDataTime = AxisData.Builder()
                .axisStepSize(8.dp)
                .backgroundColor(Color.Transparent)
                .steps(pointsDataTemp1Time.size - 1)
                .labelData { i ->
                    btUiState.graph.getOrNull(i)?.let { values ->
                        dateFormatter.format(values.timestamp) // Formatea el Date directamente
                    } ?: ""
                }
                .axisLabelAngle(90f)
                .labelAndAxisLinePadding(8.dp)
                .axisLineColor(MaterialTheme.colorScheme.tertiary)
                .axisLabelColor(MaterialTheme.colorScheme.tertiary)
                .bottomPadding(200.dp)
                .axisOffset(0.dp)
                .build()

            val xAxisDataIndex = AxisData.Builder()
                .axisStepSize(100.dp)
                .backgroundColor(Color.Transparent)
                .steps(btUiState.graph.size - 1)
                .labelData { i -> i.toString() }
                .labelAndAxisLinePadding(15.dp)
                .axisLineColor(MaterialTheme.colorScheme.tertiary)
                .axisLabelColor(MaterialTheme.colorScheme.tertiary)
                .build()

            val yAxisDataTemp = AxisData.Builder()
                .steps(40)
                .backgroundColor(Color.Transparent)
                .labelAndAxisLinePadding(20.dp)
                .labelData { i ->
                    if (i % 5 == 0) { // Mostrar etiqueta cada 5 grados
                        i.toString()
                    } else {
                        "" // No mostrar etiqueta en otros pasos
                    }
                }
                .axisLineColor(MaterialTheme.colorScheme.tertiary)
                .axisLabelColor(MaterialTheme.colorScheme.tertiary)
                .build()

            val lineChartData = LineChartData(
                linePlotData = LinePlotData(
                    lines = listOf(
                        Line(
                            dataPoints = pointsDataTemp1Time,
                            LineStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                lineType = LineType.SmoothCurve(isDotted = false)
                            ),

                            IntersectionPoint(
                                color = MaterialTheme.colorScheme.tertiary,
                                radius = 4.dp
                            ),
                            SelectionHighlightPoint(color = MaterialTheme.colorScheme.primary),
                            ShadowUnderLine(
                                alpha = 0.5f,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.inversePrimary,
                                        Color.Transparent
                                    )
                                )
                            ),
                            SelectionHighlightPopUp()
                        ),
                        Line(
                            dataPoints = pointsDataTemp2Index,
                            LineStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                lineType = LineType.SmoothCurve(isDotted = false)
                            ),
                            IntersectionPoint(
                                color = MaterialTheme.colorScheme.tertiary,
                                radius = 4.dp
                            ),
                            SelectionHighlightPoint(color = MaterialTheme.colorScheme.primary),
                            ShadowUnderLine(
                                alpha = 0.5f,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.inversePrimary,
                                        Color.Transparent
                                    )
                                )
                            ),
                            SelectionHighlightPopUp()
                        ),
                        Line(
                            dataPoints = pointsDataCurrentTime,
                            LineStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                lineType = LineType.SmoothCurve(isDotted = false)
                            ),
                            IntersectionPoint(
                                color = MaterialTheme.colorScheme.tertiary
                            ),
                            SelectionHighlightPoint(color = MaterialTheme.colorScheme.primary),
                            ShadowUnderLine(
                                alpha = 0.5f,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.inversePrimary,
                                        Color.Transparent
                                    )
                                )
                            ),
                            SelectionHighlightPopUp()
                        )
                    ),
                ),
                backgroundColor = MaterialTheme.colorScheme.surface,
                xAxisData = xAxisDataTime,
                yAxisData = yAxisDataTemp,
                gridLines = GridLines(color = MaterialTheme.colorScheme.outlineVariant)
            )

            Log.d("GraficLog", "Pre LineChart...")
            LineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                lineChartData = lineChartData
            )
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
fun DeviceDropdown(
    devices: List<BluetoothDevice>,
    selectedDevice: BluetoothDevice?,
    onDeviceSelected: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                selectedDevice?.let { getDeviceNameSafe(it, context) } ?: "Seleccionar dispositivo"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            devices.forEach { device ->
                DropdownMenuItem(
                    onClick = {
                        onDeviceSelected(device)
                        expanded = false
                    },
                    text = {
                        Text(getDeviceNameSafe(device, context))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    modifier: Modifier = Modifier,
    options: List<String>,
    label: String = "Seleccionar",
    onOptionSelected: (String) -> Unit,
    startValue: String = ""
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(startValue) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor() // Necesario para anclar correctamente el menú
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedOption = option
                        expanded = false
                        onOptionSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
fun AlarmItem(
    i: Int = 0,
    magnitud: List<String>,
    startMag: String,
    operadores: List<String> = listOf("<", ">", "="),
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
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH_SCAN
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
            context, android.Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        device.name ?: "Sin nombre"
    } else {
        "Dispositivo"
    }
}

fun transformValuesToPointData(
    valuesList: List<Values>,
    yValueSelector: (Values) -> Double,
    useIndexForX: Boolean = true
): List<Point> {
    val firstTimestampMillis = if (!useIndexForX && valuesList.isNotEmpty()) {
        valuesList.firstOrNull()?.timestamp?.time ?: 0L
    } else {
        0L
    }

    return valuesList.mapIndexed { index, values ->
        val xValue = if (useIndexForX) {
            index.toFloat()
        } else {
            val timestampMillis = values.timestamp.time
            TimeUnit.MILLISECONDS.toMinutes(timestampMillis - firstTimestampMillis).toFloat() // Ejemplo: minutos desde el inicio
        }
        Point(xValue, yValueSelector(values).toFloat())
    }
}