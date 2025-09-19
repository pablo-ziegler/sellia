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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.selliaapp.repository.CartRepository
import com.example.selliaapp.viewmodel.checkout.CheckoutViewModel


/**
 * Importante: usamos hiltViewModel() para que Hilt cree el VM con sus dependencias.
 * Si usás viewModel(), volverías a caer en el factory por defecto y repetirías el crash.
 */
@Composable
fun CheckoutScreen(
    onFinished: (String) -> Unit,
    onCancel: () -> Unit,
    vm: CheckoutViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Checkout",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.items) { item ->
                CheckoutItemRow(item)
            }
        }

        Spacer(Modifier.height(8.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Subtotal: ${formatMoney(state.subtotal)}")   // [NUEVO]
            Text("Impuestos: ${formatMoney(state.taxes)}")     // [NUEVO] (antes: state.tax)
            Text("Total: ${formatMoney(state.total)}")         // [NUEVO]
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    vm.clearCart()        // [NUEVO] limpiamos el carrito
                    onCancel()            // [NUEVO] avisamos al caller
                }
            ) { Text("Cancelar") }

            Button(
                modifier = Modifier.weight(1f),
                enabled = state.items.isNotEmpty(),
                onClick = {
                    vm.confirmSale(       // [NUEVO] usa API real del VM
                        onSuccess = { id, number ->
                            onFinished("Factura #$number (id=$id)")
                        },
                        onError = { err ->
                            onFinished("Error: ${err.message ?: "desconocido"}")
                        }
                    )
                }
            ) { Text("Confirmar") }
        }

        Spacer(Modifier.height(12.dp))
    }
}

/* [NUEVO] */
@Composable
private fun CheckoutItemRow(item: CartRepository.CartLine) {
    // Fila simple: nombre, xCantidad, precio unitario y total
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(item.name, style = MaterialTheme.typography.bodyLarge)
            Text("x${item.quantity}  ·  ${formatMoney(item.unitPrice)} c/u", style = MaterialTheme.typography.bodyMedium)
        }
        Text(formatMoney(item.lineTotal), style = MaterialTheme.typography.bodyLarge)
    }
    Spacer(Modifier.height(8.dp))
}
/* [NUEVO] */
private fun formatMoney(value: Double): String =
    "$" + String.format("%,.2f", value)