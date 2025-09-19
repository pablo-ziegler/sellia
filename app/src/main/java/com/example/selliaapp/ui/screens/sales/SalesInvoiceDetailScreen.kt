package com.example.selliaapp.ui.screens.sales


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.selliaapp.data.model.sales.InvoiceDetail
import com.example.selliaapp.data.model.sales.InvoiceItemRow
import com.example.selliaapp.viewmodel.sales.SalesInvoiceDetailViewModel
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesInvoiceDetailScreen(
    vm: SalesInvoiceDetailViewModel,
    onBack: () -> Unit
) {
    val detail by vm.state.collectAsState(initial = null)
    val currency = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
    val dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.number ?: "Detalle de factura") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (detail == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) { Text("Cargando...", style = MaterialTheme.typography.bodyMedium) }
            return@Scaffold
        }

        val d = detail!!
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Header(d, currency, dateFmt)
            }
            item { HorizontalDivider() }
            items(d.items) { item ->
                ItemRow(item, currency)
            }
            item { HorizontalDivider() }
            item {
                Row(Modifier.fillMaxWidth()) {
                    Text("Total:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(currency.format(d.total), style = MaterialTheme.typography.titleMedium)
                }
            }
            d.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Observaciones:", style = MaterialTheme.typography.titleSmall)
                    Text(notes, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun Header(
    d: InvoiceDetail,
    currency: NumberFormat,
    dateFmt: DateTimeFormatter
) {
    Column(Modifier.fillMaxWidth()) {
        Text(d.number, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text("Cliente: ${d.customerName}", style = MaterialTheme.typography.bodyLarge)
        Text("Fecha: ${d.date.format(dateFmt)}", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ItemRow(item: InvoiceItemRow, currency: NumberFormat) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text(item.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text("x${item.quantity}", style = MaterialTheme.typography.bodyMedium)
        }
        Row(Modifier.fillMaxWidth()) {
            Text(currency.format(item.unitPrice), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text(currency.format(item.lineTotal), style = MaterialTheme.typography.bodySmall)
        }
    }
}
