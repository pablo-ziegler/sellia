package com.example.selliaapp.ui.screens.manage


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.selliaapp.repository.ProductRepository
import com.example.selliaapp.viewmodel.ManageProductsViewModel
import kotlinx.coroutines.launch

@Composable
fun AddVariantScreen(
    productId: Int,
    repo: ProductRepository = hiltViewModel<ManageProductsViewModel>().let { it -> // quick access
        // Inyección directa sería mejor con @HiltViewModel propio
        // Para simplificar en este snippet tomamos repo desde VM si ya lo expones;
        // si no, crea un VM específico para variantes.
        throw NotImplementedError("Crea un ViewModel para variantes o inyecta repo con Hilt")
    }
) {
    var sku by remember { mutableStateOf("") }
    var opt1 by remember { mutableStateOf("") }
    var opt2 by remember { mutableStateOf("") }
    var qtyText by remember { mutableStateOf("0") }
    var baseText by remember { mutableStateOf("") }
    var taxText by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(Modifier
        .verticalScroll(rememberScrollState())
        .imePadding()
        .navigationBarsPadding()
        .padding(16.dp),
         verticalArrangement = Arrangement.spacedBy(12.dp))
    {
        OutlinedTextField(sku, { sku = it }, label = { Text("SKU variante") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(opt1, { opt1 = it }, label = { Text("Opción 1 (ej. Talle)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(opt2, { opt2 = it }, label = { Text("Opción 2 (ej. Color)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(qtyText, { qtyText = it.filter { ch -> ch.isDigit() } }, label = { Text("Stock") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(baseText, { baseText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } }, label = { Text("Base price") })
        OutlinedTextField(taxText, { taxText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } }, label = { Text("IVA (%)") })

        Button(onClick = {
            val qty = qtyText.toIntOrNull() ?: 0
            val base = baseText.replace(',', '.').toDoubleOrNull()
            val tax = taxText.replace(',', '.').toDoubleOrNull()?.div(100.0)
            scope.launch {
                // repo.createVariant(...)
                // ver nota en el constructor; idealmente usá un VM con @Inject repo
            }
        }) { Text("Crear variante") }
    }
}
