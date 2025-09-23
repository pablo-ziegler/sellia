// [NUEVO] Archivo completo: CheckoutScreen.kt
package com.example.selliaapp.ui.screens.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.selliaapp.ui.navigation.Routes.SellRoutes.SELL_FLOW_ROUTE
import com.example.selliaapp.viewmodel.SellViewModel


/**
 * Importante: usamos hiltViewModel() para que Hilt cree el VM con sus dependencias.
 * Si usás viewModel(), volverías a caer en el factory por defecto y repetirías el crash.
 */
@Composable
fun CheckoutScreen(
     navController: NavController,
     onCancel : () -> Unit,
) {


    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val parentEntry = remember(navBackStackEntry) {
        navController.getBackStackEntry(SELL_FLOW_ROUTE)
    }

    val vm: SellViewModel = hiltViewModel(parentEntry)
    val state by vm.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Confirmar venta",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        if (state.items.isEmpty()) {
            Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        "No hay productos en el carrito.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { navController.popBackStack() }) {
                        Text("Volver a vender")
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f, fill = true)) {
                items(state.items) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(item.name ?: "-", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "x${item.qty} · $${"%.2f".format(item.unitPrice)} c/u",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text("$${"%.2f".format(item.unitPrice * item.qty)}", style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", style = MaterialTheme.typography.headlineSmall)
                Text("$${"%.2f".format(state.total)}", style = MaterialTheme.typography.headlineSmall)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Volver")
            }
            Button(
                onClick = {
                    // Valida y limpia el carrito
                    vm.placeOrder()
                    // Salir del checkout
                    navController.popBackStack()
                },
                enabled = state.items.isNotEmpty() && state.stockViolations.isEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Finalizar")
            }
        }
    }
}


/* [NUEVO] */
private fun formatMoney(value: Double): String =
    "$" + String.format("%,.2f", value)