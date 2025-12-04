# Documentación Técnica: Indicadores de Negocio y Origen de Datos

Este documento detalla el origen de los datos y la lógica de cálculo para cada uno de los indicadores presentados en el reporte administrativo.

## 1. Solicitudes a Base de Datos

El sistema utiliza **Spring Data JPA** para interactuar con la base de datos MySQL. Las solicitudes principales se realizan a través de los siguientes repositorios:

### A. Ventas (`CompraRepository`)
- **Método**: `findByFechaBetween(LocalDateTime start, LocalDateTime end)`
- **Uso**: Obtiene todas las transacciones de venta dentro del rango de fechas seleccionado.
- **Datos obtenidos**: ID, Fecha, Usuario, Producto, Cantidad, Total.

### B. Alquileres (`AlquilerRepository`)
- **Método**: `findByFechaRegistroBetween(LocalDateTime start, LocalDateTime end)`
- **Uso**: Obtiene todos los registros de alquiler dentro del rango de fechas.
- **Datos obtenidos**: ID, Fecha Registro, Fecha Fin, Usuario, Producto, Cantidad, Total, Estado, Días.

### C. Inventario (`ProductoRepository`)
- **Método**: `findAll()`
- **Uso**: Obtiene la lista completa de productos para calcular el valor del stock y la ocupación.
- **Datos obtenidos**: Stock, Precio Venta, Tipo (Venta/Alquiler).

### D. Actividad Admin (`ReporteRepository`)
- **Método**: `findByFechaBetweenOrderByFechaDesc`
- **Uso**: Obtiene el registro de acciones administrativas (creación/eliminación) para auditoría.

---

## 2. Cálculo de Indicadores (KPIs)

A continuación se describe cómo se obtiene cada indicador en `ReportService.java`.

### 📊 Indicadores Generales
| Indicador | Fuente de Datos | Lógica de Cálculo |
|-----------|-----------------|-------------------|
| **Ventas Totales** | Base de Datos (Real) | Suma del campo `total` de todas las ventas en el rango. |
| **Alquileres Totales** | Base de Datos (Real) | Suma del campo `total` de todos los alquileres en el rango. |
| **Transacciones** | Base de Datos (Real) | Conteo simple de registros (`size()`) de las listas de ventas y alquileres. |

### 🌐 Indicadores Externos
| Indicador | Fuente de Datos | Lógica de Cálculo |
|-----------|-----------------|-------------------|
| **Tasa de Conversión** | Estimación | `(Ventas + Alquileres) / Visitas Estimadas`. Las visitas se estiman como `(Transacciones * 5)`, asumiendo una conversión del 20%. |
| **Satisfacción Cliente** | Simulado (Mock) | Valor fijo "4.5/5.0" (Requiere implementación de sistema de encuestas). |
| **Ocupación Inventario** | Híbrido (Real/Aprox) | `(Alquileres Activos / Stock Total Rentable) * 100`. Los alquileres activos se filtran por estado "activo". |

### 📉 Gestión y Pérdidas
| Indicador | Fuente de Datos | Lógica de Cálculo |
|-----------|-----------------|-------------------|
| **Rotación Inventario** | Proxy (Real) | `Cantidad Ventas / Stock Total`. Indica cuántas veces se renueva el inventario en el periodo. |
| **Porcentaje de Pérdidas** | Estimación | Se asume un 1% del valor total del inventario (`Stock * Precio`) como pérdida estimada por mermas o daños. |
| **Tiempo Reposición** | Simulado (Mock) | Valor fijo "3 días". |

### 💰 Financieros y Crecimiento
| Indicador | Fuente de Datos | Lógica de Cálculo |
|-----------|-----------------|-------------------|
| **Crecimiento Ventas** | Base de Datos (Real) | Comparación porcentual entre las ventas del periodo actual y el periodo anterior de igual duración. Fórmula: `((Actual - Anterior) / Anterior) * 100`. |
| **Margen Utilidad** | Estimación | Valor fijo "30%" (Requiere campo de `precio_costo` en productos para cálculo real). |
| **Tasa Morosidad** | Base de Datos (Real) | `(Alquileres Atrasados / Total Alquileres) * 100`. Se consideran atrasados los que tienen estado "atrasado" o fecha fin vencida. |

### 👥 Clientes y Procesos
| Indicador | Fuente de Datos | Lógica de Cálculo |
|-----------|-----------------|-------------------|
| **Nuevos vs Recurrentes** | Simulado (Mock) | Valor fijo "80% / 20%". |
| **Duración Prom. Alquiler** | Base de Datos (Real) | Promedio del campo `dias` de todos los alquileres en el rango. |
| **Eficiencia Facturación** | Simulado (Mock) | Valor fijo "98%". |
| **Cumplimiento Cronogramas** | Derivado (Real) | `100% - Tasa de Morosidad`. Refleja el porcentaje de alquileres devueltos a tiempo. |

### 🏆 Mercado y Calidad
| Indicador | Fuente de Datos | Lógica de Cálculo |
|-----------|-----------------|-------------------|
| **Participación Mercado** | Simulado (Mock) | Valor fijo "15%". |
| **Demanda Insatisfecha** | Simulado (Mock) | Valor fijo "5%". |
| **Tasa Devoluciones** | Simulado (Mock) | Valor fijo "2%". |

## Notas Importantes
- **Datos Reales**: Los indicadores financieros, de inventario y crecimiento se basan en datos reales de las transacciones del sistema.
- **Datos Simulados (Mock)**: Algunos indicadores cualitativos o de mercado (Satisfacción, Participación) utilizan valores fijos ya que el sistema actual no recolecta esa información específica.
- **Estimaciones**: La conversión y pérdidas utilizan fórmulas basadas en supuestos de negocio estándar para proveer métricas útiles sin datos granulares.
