package com.example.refrimed.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.refrimed.R
import com.example.refrimed.data.Screen
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

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
    val deviceConfig by btViewModel.deviceConfig.collectAsState(initial = "")

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
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bettaingenieria.tech/refrimed/refrimed.html"))
                    context.startActivity(intent)
                }
            )
            CardItem(
                "Notas útiles",
                R.drawable.refrimed_notes,
                "Apartado con utilidades y notas técnicas",
                onClick = { refrimedViewModel.setScreen(Screen.NOTES) }
            )
        }

        if (refrimedUiState.screen == Screen.BLUETOOTH) {
            BluetoothScreen(refrimedViewModel, btViewModel, btUiState, connectedSocket, deviceConfig)
        }

        if (refrimedUiState.screen == Screen.BLUETOOTH_DEVICE_CONFIG) {
            BluetoothConfigScreen(btViewModel, btUiState)
            if (connectedSocket == null) refrimedViewModel.setScreen(Screen.BLUETOOTH)
        }

        if (refrimedUiState.screen == Screen.BLUETOOTH_THRESHOLDS_CONFIG) {
            BluetoothThresholdsScreen(btViewModel, btUiState)
            if (connectedSocket == null) refrimedViewModel.setScreen(Screen.BLUETOOTH)
        }

        if (refrimedUiState.screen == Screen.BLUETOOTH_GRAFICO) {
            GraficoScreen(btViewModel, btUiState)
            if (connectedSocket == null) refrimedViewModel.setScreen(Screen.BLUETOOTH)
        }

        if (refrimedUiState.screen == Screen.CLOUD) {
            TODO()
        }
    }
}