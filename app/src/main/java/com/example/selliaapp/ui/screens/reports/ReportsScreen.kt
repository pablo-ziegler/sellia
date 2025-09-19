package com.example.selliaapp.ui.screens.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.selliaapp.ui.navigation.Routes
import com.example.selliaapp.viewmodel.ReportsViewModel
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer

/**
 * Pantalla de reportes:
 * Muestra filtro, total y gráfico de líneas.
 */
@Composable
fun ReportsScreen(
    vm: ReportsViewModel = hiltViewModel(),
    onBack: () -> Boolean,
    navController: NavController,


    ) {
    val state by vm.state.collectAsState()

    Column(Modifier
        .verticalScroll(rememberScrollState())
        .imePadding()
        .navigationBarsPadding()
        .padding(16.dp)
        .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp))
    {
        Text("Reportes", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        FilterChips(
            onDay = { vm.onFilterChange("Día") },
            onWeek = { vm.onFilterChange("Semana") },
            onMonth = { vm.onFilterChange("Mes") }
        )
        Spacer(Modifier.height(16.dp))
        Text("Total: ${state.total}")

        if (state.points.isNotEmpty()) {
            // Mapeo de pares (label, value) -> puntos para el chart
            val points = remember(state.points) {
                state.points.map { (label, value) ->
                    LineChartData.Point(value.toFloat(), label)
                }
            }

            // IMPORTANTE:
            // En ciertas versiones, LineChartData acepta solo 'points' (y opcionalmente 'lineDrawer').
            // 'pointDrawer' NO está disponible en ese constructor -> se remueve para compilar.
            val lineData = LineChartData(
                points = points,
                lineDrawer = SolidLineDrawer()
                // pointDrawer = FilledCircularPointDrawer() // <- NO DISPONIBLE en tu versión
            )

            LineChart(
                linesChartData = listOf(lineData),
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        } else {
            Text("Sin datos en el rango seleccionado")
        }
        Button(onClick = { navController.navigate(Routes.SalesInvoices.route) }) {
            Text("Ver facturas de venta")
        }
    }
}

@Composable
private fun FilterChips(
    onDay: () -> Unit,
    onWeek: () -> Unit,
    onMonth: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(onClick = onDay, label = { Text("Día") })
        AssistChip(onClick = onWeek, label = { Text("Semana") })
        AssistChip(onClick = onMonth, label = { Text("Mes") })
    }
}
