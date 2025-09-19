// File: HomeScreen.kt
package com.example.selliaapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.selliaapp.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla principal con búsqueda, accesos rápidos y listado de ventas recientes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onNewSale: () -> Unit,
    onStock: () -> Unit,
    onClientes: () -> Unit,
    onConfig: () -> Unit,
    onReports: () -> Unit,
    onProviders: () -> Unit,          // NUEVO
    onExpenses: () -> Unit,
    onSyncNow: () -> Unit = {}
    ) {
    val state by vm.state.collectAsState()
    val currency = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    Box(modifier = Modifier.fillMaxSize()) {

        // -------- Contenido superior (franja + botones) ----------
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .fillMaxSize()
                .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {
            ElevatedCard {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Total vendido (mes)",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (state.isLoading) "Cargando…" else currency.format(state.monthTotal),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }

            Card(elevation = CardDefaults.cardElevation(2.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ShowChart, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ventas de la semana")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Botonera superior (puedes ajustar layout si querés 3 por fila)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onStock, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.ViewList, contentDescription = null)
                        Spacer(Modifier.width(8.dp)); Text("Stock")
                    }
                    OutlinedButton(onClick = onClientes, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Business, contentDescription = null)
                        Spacer(Modifier.width(8.dp)); Text("Clientes")
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onConfig, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(Modifier.width(8.dp)); Text("Configuración")
                    }
                    OutlinedButton(onClick = onReports, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.InsertChart, contentDescription = null)
                        Spacer(Modifier.width(8.dp)); Text("Reportes")
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onProviders, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Business, contentDescription = null)
                        Spacer(Modifier.width(8.dp)); Text("Proveedores")
                    }
                    OutlinedButton(onClick = onExpenses, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null)
                        Spacer(Modifier.width(8.dp)); Text("Administración Gastos")
                    }
                }
                // El resto del espacio hasta el botón inferior queda libre
                Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onSyncNow, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(Modifier.width(8.dp)); Text("Sincronizar Ahora!")
                    }
                }
            }
        }

        // -------- Botón VENDER: franja inferior ocupando 1/4 de la pantalla ----------
        Button(
            onClick = onNewSale,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.25f) // ocupa 1/4 de la altura total
                .padding(0.dp),       // sin padding para que sea de borde a borde
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    "VENDER",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}