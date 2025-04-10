package com.example.refrimed.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.refrimed.R
import com.example.refrimed.data.RefrimedUiState
import com.example.refrimed.data.Screen

@Composable
fun RefrimedHome(
    contentPadding: PaddingValues,
    refrimedUiState: RefrimedUiState,
    viewModel: RefrimedViewModel
) {
    Column {
        CardItem(
            "Conexión bluetooth",
            R.drawable.refrimed_bluetooth,
            "Las configuraciones en el equipo solo se hacen mediante este método de conexión",
            onClick = { viewModel.setScreen(Screen.BLUETOOTH) }
        )
        CardItem(
            "Conexión remota",
            R.drawable.refrimed_cloud,
            "Acceso a mediciones y registros desde la base de datos de Betta Ingeniería",
            onClick = { viewModel.setScreen(Screen.CLOUD) }
        )
        CardItem(
            "Notas útiles",
            R.drawable.refrimed_notes,
            "Apartado con utilidades y notas técnicas",
            onClick = { viewModel.setScreen(Screen.NOTES) }
        )
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
            .padding(horizontal = 16.dp, vertical = 32.dp)
            .clickable { onClick() }
    ) {
        val height = 160
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

//@Composable
//@Preview
//fun Preview1() {
//    CardItem("Conexión bluetooth", R.drawable.refrimed_bluetooth, "Las configuraciones en el equipo solo se hacen mediante este método de conexión")
//}
//
//@Composable
//@Preview
//fun Preview2() {
//    CardItem("Conexión remota", R.drawable.refrimed_cloud, "Acceso a mediciones y registros desde la base de datos de Betta Ingeniería")
//}
//
//@Composable
//@Preview
//fun Preview3() {
//    CardItem("Notas útiles", R.drawable.refrimed_notes, "Apartado con utilidades y notas técnicas")
//}