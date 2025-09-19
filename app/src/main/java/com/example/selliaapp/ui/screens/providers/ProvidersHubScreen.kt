package com.example.selliaapp.ui.screens.providers


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProvidersHubScreen(
    onManageProviders: () -> Unit,
    onProviderInvoices: () -> Unit,
    onProviderPayments: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onManageProviders) { Text("Gestionar Proveedores (CRUD)") }
        Button(onClick = onProviderInvoices) { Text("Boletas/Facturas por Proveedor") }
        Button(onClick = onProviderPayments) { Text("Pagos a Proveedores (Pendientes)") }
        OutlinedButton(onClick = onBack) { Text("Volver") }
    }
}
