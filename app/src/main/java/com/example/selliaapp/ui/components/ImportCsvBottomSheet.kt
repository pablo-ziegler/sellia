package com.example.selliaapp.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp

/**
 * Hoja inferior que explica el formato de importación y
 * lanza el picker de CSV (compatible con exportación de Excel/Sheets).
 *
 * Ventaja: sin libs externas (POI) y confiable. Excel/Sheets exportan a CSV fácilmente.
 */
@Composable
fun ImportCsvBottomSheet(
    onPickCsv: (Uri) -> Unit
) {
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> if (uri != null) onPickCsv(uri) }
    )

    val template = remember {
        buildAnnotatedString {
            appendLine("Plantilla CSV (separador coma ,):")
            appendLine("code,barcode,name,quantity,price,category,min_stock")
            appendLine("SKU-001,7791234567890,Shampoo 500ml,24,2500.00,Higiene,5")
            appendLine("SKU-002,,Jabón Neutro x3,50,1800.00,Higiene,10")
        }.toString()
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Importar stock desde CSV", style = MaterialTheme.typography.titleMedium)
        Text(
            "Podés exportar desde Excel o Google Sheets a CSV y cargarlo acá. " +
                    "Las columnas aceptadas son (en este orden):"
        )
        HorizontalDivider()
        Text(template, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { picker.launch("text/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Seleccionar archivo CSV")
        }
    }
}
