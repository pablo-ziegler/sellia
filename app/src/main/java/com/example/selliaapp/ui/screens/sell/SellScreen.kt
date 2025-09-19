package com.example.selliaapp.ui.screens.sell

/* [ANTERIOR]
package com.example.selliaapp.ui.screens.sell

<< PEGASTE ESTA VERSIÓN (con referencias a state.cart / state.error / state.lastInvoiceId).
   La conservo como bloque comentado para que compares. >>
... (TU ARCHIVO ANTERIOR COMPLETO) ...
*/

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.selliaapp.data.local.entity.ProductEntity
import com.example.selliaapp.ui.components.ProductPickerSheet
import com.example.selliaapp.ui.components.QuantityInputDialog
import com.example.selliaapp.ui.navigation.Routes
import com.example.selliaapp.viewmodel.CustomersViewModel
import com.example.selliaapp.viewmodel.ProductViewModel
import com.example.selliaapp.viewmodel.SellViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla de ventas (unificada a `ui`):
 * - Usa SOLO `ui` = sellVm.state.collectAsState() (no referencias a state.cart/error/lastInvoiceId)
 * - Recalcula remanentes y total desde `ui.items`
 * - Flujo de escaneo: si no existe → AddProduct con barcode precargado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellScreen(
    sellVm: SellViewModel = hiltViewModel(),
    productVm: ProductViewModel = hiltViewModel(),
    customersVm: CustomersViewModel = hiltViewModel(),
    onScanClick: () -> Unit,
    onBack: () -> Boolean,
    navController: NavController
) {
    val ui by sellVm.state.collectAsState()
    val allProducts by productVm.products.collectAsState(initial = emptyList())
    val currency = remember { NumberFormat.getCurrencyInstance(Locale("es", "AR")) }


    // 👉 Agrego SnackbarHostState y CoroutineScope para mostrar mensajes (FIX a "Unresolved reference")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    // Stock remanente por producto (stock total - cantidad en carrito)
    val remainingById = remember(ui.items, allProducts) {
        val qtyById = ui.items.associate { it.productId to it.qty }
        allProducts.associate { p ->
            val inCart = qtyById[p.id] ?: 0
            p.id to (p.quantity - inCart).coerceAtLeast(0)
        }
    }

    // Picker + diálogo
    var showPicker by remember { mutableStateOf(false) }
    var askFor by remember { mutableStateOf<ProductEntity?>(null) }

    if (showPicker) {
        ProductPickerSheet(
            products = allProducts.filter { (remainingById[it.id] ?: 0) > 0 },
            onPick = { p -> showPicker = false; askFor = p },
            onDismiss = { showPicker = false }
        )
    }

    askFor?.let { product ->
        val current = ui.items.firstOrNull { it.productId == product.id }?.qty ?: 0
        val initial = if (current > 0) current else 1
        val remaining = remainingById[product.id] ?: product.quantity

        QuantityInputDialog(
            title = if (current > 0) "Nueva cantidad" else "Cantidad a vender",
            initialValue = initial,
            maxValue = remaining,                 // << límite de stock para validación live
            confirmText = if (current > 0) "Actualizar" else "Agregar",
            cancelText = "Cancelar",
            onConfirm = { qty ->
                if (current > 0) sellVm.updateQty(product.id, qty) else sellVm.addToCart(product, qty)
                askFor = null
            },
            onCancel = { askFor = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Venta", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onScanClick) {
                Icon(Icons.Default.Add, contentDescription = "Escanear/Agregar")
            }
        },
        // 👉 Conecto el snackbarHostState al Scaffold (antes se creaba ad-hoc y no era accesible)
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(12.dp)
        ) {
            TextButton(onClick = { showPicker = true }) { Text("Agregar producto") }
            Spacer(Modifier.height(8.dp))

            if (ui.items.isEmpty()) {
                Text("No hay productos en el carrito.")
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                    items(ui.items, key = { it.productId }) { item ->
                        val canIncrease = item.qty < item.maxStock
                        val atMax = item.qty == item.maxStock && item.maxStock > 0
                        CartItemRow(
                            name = item.name,
                            barcode = item.barcode,
                            unitPrice = item.unitPrice,
                            qty = item.qty,
                            maxStock = item.maxStock,
                            lineTotal = item.lineTotal,
                            canIncrease = canIncrease,
                            onIncrease = { sellVm.increment(item.productId) },
                            onDecrease = { sellVm.decrement(item.productId) },
                            onRemove = { sellVm.remove(item.productId) },
                            onEdit = { askFor = allProducts.firstOrNull { it.id == item.productId } },
                            currency = currency,
                            showAtMaxHint = atMax
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    // Resumen de totales
                    item {
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                                    Text(currency.format(ui.subtotal), style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(Modifier.height(4.dp))
                                Divider(Modifier.padding(vertical = 4.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total", style = MaterialTheme.typography.titleMedium)
                                    Text(currency.format(ui.total), style = MaterialTheme.typography.titleMedium)
                                }
                                if (!ui.canCheckout) {
                                    Spacer(Modifier.height(6.dp))
                                    Text("Hay cantidades inválidas en el carrito.", color = Color.Red)
                                }
                            }
                        }
                    }

                    // Botón: Scanear producto
                    item {
                        Button(
                            onClick = onScanClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.PointOfSale, contentDescription = null)
                                Spacer(Modifier.width(12.dp))
                                Text("Scanear producto", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    // Botón: Vender
                    item {
                        Button(
                            onClick = {
                                if (!ui.canCheckout) {
                                    scope.launch {
                                        val first = ui.stockViolations.entries.firstOrNull()
                                        val msg = if (first != null) {
                                            "Stock insuficiente en al menos un producto (disponible: ${first.value})."
                                        } else {
                                            "Agregá productos para continuar."
                                        }
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                } else {
                                    // 👉 Ir al flujo de cobro que ya tenés implementado
                                    navController.navigate(Routes.Checkout.route)
                                }
                            },
                            enabled = ui.items.isNotEmpty(), // si hay items
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Finalizar venta")
                        }
                    }
                }
            }
        }
    }
}


    /** Fila de ítem con +/− de a 1 y feedback visual inmediato. */
@Composable
fun CartItemRow(
    name: String,
    barcode: String?,
    unitPrice: Double,
    qty: Int,
    maxStock: Int,
    lineTotal: Double,
    canIncrease: Boolean,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    onEdit: () -> Unit,
    currency: NumberFormat,
    showAtMaxHint: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (!barcode.isNullOrBlank()) {
                        Text("Código: $barcode", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") }
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrease, enabled = qty > 1) {
                        Icon(Icons.Default.Remove, contentDescription = "Menos")
                    }
                    val qtyColor = when {
                        qty > maxStock  -> Color.Red
                        showAtMaxHint   -> Color(0xFFCC7700)
                        else            -> MaterialTheme.colorScheme.onSurface
                    }
                    Text("$qty", style = MaterialTheme.typography.titleSmall, color = qtyColor)
                    IconButton(onClick = onIncrease, enabled = canIncrease) {
                        Icon(Icons.Default.Add, contentDescription = "Más")
                    }
                    TextButton(onClick = onEdit, modifier = Modifier.padding(start = 8.dp)) { Text("Editar") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(currency.format(unitPrice), modifier = Modifier.padding(end = 12.dp))
                    Text(currency.format(lineTotal), style = MaterialTheme.typography.titleSmall)
                }
            }

            if (qty > maxStock) {
                Text("Cantidad supera el stock (máx: $maxStock).", color = Color.Red)
            } else if (showAtMaxHint) {
                Text("Llegaste al stock máximo disponible.", color = Color(0xFFCC7700))
            }
        }
    }
}


