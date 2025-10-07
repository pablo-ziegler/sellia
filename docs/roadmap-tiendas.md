# Roadmap de evolución para Sellia

Este documento resume oportunidades de mejora sobre la aplicación de gestión de tiendas y propone mockups de referencia para iterar la experiencia.

## 1. Prioridades de evolución funcional

### 1.1 Dashboard accionable y centrado en objetivos
- **Contexto actual:** La pantalla `HomeScreen` ya muestra el total vendido en el mes, el detalle diario de las últimas ventas y accesos rápidos a stock, clientes, configuración, reportes, proveedores, gastos y sincronización, rematando con un botón de venta destacado.【F:app/src/main/java/com/example/selliaapp/ui/screens/HomeScreen.kt†L74-L213】
- **Implementación reciente:** El tablero incorpora alertas de stock crítico y un resumen de ventas de los últimos siete días alimentado por `HomeViewModel`, combinando métricas y series diarias en una sola vista.【F:app/src/main/java/com/example/selliaapp/viewmodel/HomeViewModel.kt†L18-L87】【F:app/src/main/java/com/example/selliaapp/repository/impl/InvoiceRepositoryImpl.kt†L124-L162】
- **Mejora propuesta:** Transformar la tarjeta de métricas en un panel modular con KPIs configurables (ventas diarias, margen, tickets medios) y un carrusel de tareas pendientes (reposición de stock, facturas vencidas). Permite que el dashboard se adapte a distintos roles (dueño vs. cajero) y priorice acciones del día.

### 1.2 Flujo de venta inteligente y omnicanal
- **Contexto actual:** El flujo `Routes.Sell` integra escaneo, búsqueda y carrito con control de stock en vivo, compartiendo estado con `CheckoutScreen` en el mismo grafo de navegación.【F:app/src/main/java/com/example/selliaapp/ui/navigation/SelliaApp.kt†L148-L199】【F:app/src/main/java/com/example/selliaapp/ui/screens/sell/SellScreen.kt†L86-L189】
- **Implementación reciente:** La pantalla de venta permite aplicar descuentos y recargos en tiempo real, recalculando el total desde `SellViewModel`, mientras que el checkout incorpora selector de método de pago, notas y confirmación con snackbar.【F:app/src/main/java/com/example/selliaapp/ui/screens/sell/SellScreen.kt†L180-L238】【F:app/src/main/java/com/example/selliaapp/viewmodel/SellViewModel.kt†L150-L215】【F:app/src/main/java/com/example/selliaapp/ui/screens/checkout/CheckoutScreen.kt†L120-L242】
- **Mejora propuesta:**
  - Agregar promociones dinámicas (3x2, descuentos por cliente) visibles al seleccionar productos.
  - Incluir venta rápida por catálogo offline y pedidos diferidos (reserva para retiro o envío) aprovechando la estructura existente de carrito.
  - Incorporar resumen de cliente en checkout (saldo, historial) para cross-selling.

### 1.3 Planeamiento de stock y abastecimiento
- **Contexto actual:** `ProductRepository` normaliza categorías/proveedores, realiza importaciones CSV y sincroniza con Firestore, mientras que la navegación ofrece hubs dedicados para proveedores.【F:app/src/main/java/com/example/selliaapp/repository/ProductRepository.kt†L40-L199】【F:app/src/main/java/com/example/selliaapp/ui/navigation/SelliaApp.kt†L101-L122】【F:app/src/main/java/com/example/selliaapp/ui/screens/providers/ProvidersHubScreen.kt†L26-L37】
- **Mejora propuesta:**
  - Generar reportes de rotación y proyección de compras automáticamente desde importaciones y ventas recientes.
  - Añadir alertas de stock crítico en dashboard y en flujo de compra a proveedores, con sugerencias de cantidades.
  - Integrar órdenes de compra digitales que puedan sincronizarse con proveedores o exportarse.

### 1.4 Control integral de gastos y flujo de caja
- **Contexto actual:** Existe un hub de gastos con accesos a plantillas y carga/listado, respaldado por `ExpenseRepository` y DAOs dedicados.【F:app/src/main/java/com/example/selliaapp/ui/screens/expenses/ExpensesHubScreen.kt†L19-L36】【F:app/src/main/java/com/example/selliaapp/di/AppModule.kt†L88-L181】
- **Mejora propuesta:**
  - Permitir presupuestación mensual por categoría y proyecciones comparando gastos vs. ventas.
  - Añadir anexos (fotos de tickets) y aprobación de gastos para equipos.
  - Exponer reportes de cashflow combinando ventas, gastos y pagos a proveedores.

### 1.5 Configuración empresarial y multi-sucursal
- **Contexto actual:** El módulo de dependencias centraliza repositorios y DAOs, lo que facilita escalar a nuevas entidades y sincronizaciones.【F:app/src/main/java/com/example/selliaapp/di/AppModule.kt†L57-L209】
- **Mejora propuesta:**
  - Incorporar soporte multi-sucursal con catálogo compartido y stock separado por ubicación.
  - Añadir roles y permisos finos (dueño, cajero, reponedor) desde el módulo de configuración.
  - Sincronizar configuraciones con Firestore para mantener paridad entre dispositivos.

## 2. Roadmap sugerido por fases

1. **Fase 1 – Métricas accionables (2-3 sprints):**
   - KPIs configurables en el dashboard.
   - Alertas de stock crítico y tareas rápidas.
   - Exportación de reportes clave para dirección.

2. **Fase 2 – Ventas y stock avanzados (3-4 sprints):**
   - Promociones y reglas de precios.
   - Pedidos diferidos y logística ligera.
   - Generación de órdenes de compra con proyecciones.

3. **Fase 3 – Gobernanza financiera (2-3 sprints):**
   - Presupuestos de gastos y flujos de aprobación.
   - Cashflow unificado (ventas + gastos + proveedores).
   - Integraciones contables (export a CSV/ERP).

4. **Fase 4 – Multi-sucursal y roles (4+ sprints):**
   - Inventario por sucursal, transferencia interna.
   - Roles avanzados y auditoría de operaciones.
   - Sincronización remota resiliente (offline-first con colas).

## 3. Mockups de referencia (low-fi)

> Los siguientes wireframes en ASCII ayudan a visualizar ajustes en la UI. Sirven como punto de partida para sesiones de diseño de alta fidelidad.

### 3.1 Dashboard con KPIs y tareas
```
┌───────────────────────────────────────────────┐
│ Ventas hoy        Margen bruto      Ticket 🌟 │
│ $125.430          38 %              $3.240     │
├───────────────────────────────────────────────┤
│ Alertas                                            │
│ • Reponer: Yerba Canarias (stock < mín)            │
│ • Factura Proveedor "Distribuidora Sur" vence hoy │
├───────────────────────────────────────────────┤
│ Acciones rápidas                                 │
│ [Nueva venta] [Stock] [Pedidos] [Gastos] [Reportes] │
│ [Proveedores] [Sincronizar]                       │
└───────────────────────────────────────────────┘
```

### 3.2 Flujo de venta con promociones
```
┌─────────────── Venta ─────────────────┐
│ Cliente: Ana Gómez (Saldo $1.200)     │
├───────────────────────────────────────┤
│ Producto              Cant  Precio    │
│ Café Tostado 1kg      2     $8.000    │
│  ↳ Promo 2x1 aplicada (-$8.000)       │
│ Leche Almendra 1L     1     $2.900    │
├───────────────────────────────────────┤
│ Subtotal                         $9.800 │
│ Descuentos                      -$8.000 │
│ Total                            $1.800 │
├───────────────────────────────────────┤
│ [Agregar producto]  [Escanear código] │
│ [Reservar envío]    [Cobrar]          │
└───────────────────────────────────────┘
```

### 3.3 Planificación de compra a proveedores
```
┌────────── Pedido sugerido ──────────┐
│ Proveedor: Distribuidora Sur        │
├─────────────────────────────────────┤
│ Producto          Stock  Min  Suger. │
│ Yerba Canarias     4      12   16     │
│ Café Brasil        6       8   10     │
│ Azúcar 1kg         9      15   12     │
├─────────────────────────────────────┤
│ Reposición recomendada: 38 unidades │
│ Costo estimado: $52.400              │
│ [Generar orden] [Exportar PDF]       │
└─────────────────────────────────────┘
```

## 4. Próximos pasos

1. Alinear las prioridades con stakeholders (dueños, encargados, cajeros).
2. Refinar mockups con diseño visual y validar con usuarios clave.
3. Estimar esfuerzo técnico junto a los equipos de backend y mobile.
4. Definir experimentos de medición (ventas, rotación, satisfacción) para evaluar impacto.

---
Este roadmap busca evolucionar Sellia hacia una plataforma integral para tiendas físicas y omnicanal, maximizando valor en cada iteración.
