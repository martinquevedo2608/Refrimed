package com.example.refrimed.ui

import android.Manifest
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
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily

import com.example.refrimed.data.AlarmData
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
import com.patrykandpatrick.vico.core.cartesian.Zoom
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
import java.util.Date
import java.util.Locale

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
            .fillMaxSize()
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

