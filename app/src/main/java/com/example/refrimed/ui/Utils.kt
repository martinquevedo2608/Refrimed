package com.example.refrimed.ui

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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