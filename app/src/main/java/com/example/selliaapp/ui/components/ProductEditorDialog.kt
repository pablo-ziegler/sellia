package com.example.selliaapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.example.selliaapp.data.local.entity.ProductEntity

@Composable
fun ProductEditorDialog(
    initial: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, barcode: String, price: Double, stock: Int, description: String?) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(initial?.name.orEmpty())) }
    var barcode by remember { mutableStateOf(TextFieldValue(initial?.barcode.orEmpty())) }
    var price by remember { mutableStateOf(TextFieldValue(initial?.price?.toString() ?: "")) }
    var stock by remember { mutableStateOf(TextFieldValue(initial?.quantity?.toString() ?: "")) }
    var description by remember { mutableStateOf(TextFieldValue(initial?.description ?: "")) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") })
                OutlinedTextField(barcode, { barcode = it }, label = { Text("Código") })
                OutlinedTextField(price, { price = it }, label = { Text("Precio") })
                OutlinedTextField(stock, { stock = it }, label = { Text("Stock") })
                OutlinedTextField(description, { description = it }, label = { Text("Descripción") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val p = price.text.toDoubleOrNull() ?: 0.0
                val s = stock.text.toIntOrNull() ?: 0
                onSave(name.text.trim(), barcode.text.trim(), p, s, description.text.trim().ifBlank { null })
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
