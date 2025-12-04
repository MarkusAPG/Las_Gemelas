package com.lasgemelas.service;

import com.lasgemelas.model.Alquiler;
import com.lasgemelas.model.Compra;
import com.lasgemelas.model.Producto;
import com.lasgemelas.repository.AlquilerRepository;
import com.lasgemelas.repository.CompraRepository;
import com.lasgemelas.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private AlquilerRepository alquilerRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // 1. & 2. Daily Sales and Rentals (Date specific)
    public List<Compra> getDailySales(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return compraRepository.findByFechaBetween(start, end);
    }

    public List<Compra> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return compraRepository.findByFechaBetween(start, end);
    }

    public List<Alquiler> getDailyRentals(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return alquilerRepository.findByFechaRegistroBetween(start, end);
    }

    public List<Alquiler> getRentalsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return alquilerRepository.findByFechaRegistroBetween(start, end);
    }

    // 1. Inventory Control (All products)
    public List<Producto> getInventory() {
        return productoRepository.findAll();
    }

    // 3. Indicators
    public Map<String, Object> getIndicators(LocalDate startDate, LocalDate endDate) {
        List<Compra> sales = getSalesByDateRange(startDate, endDate);
        List<Alquiler> rentals = getRentalsByDateRange(startDate, endDate);
        List<Producto> inventory = getInventory();

        BigDecimal totalSales = sales.stream()
                .map(c -> c.getTotal() != null ? c.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRentals = rentals.stream()
                .map(a -> a.getTotal() != null ? a.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> indicators = new HashMap<>();
        indicators.put("totalSalesAmount", totalSales);
        indicators.put("totalRentalsAmount", totalRentals);
        indicators.put("salesCount", sales.size());
        indicators.put("rentalsCount", rentals.size());

        // --- Indicadores Externos ---
        // Conversion: (Sales / Visits) * 100. Mocking visits as Sales * 5 (20%
        // conversion)
        int estimatedVisits = (sales.size() + rentals.size()) * 5;
        double conversionRate = estimatedVisits > 0 ? ((double) (sales.size() + rentals.size()) / estimatedVisits) * 100
                : 0;
        indicators.put("conversionRate", String.format("%.2f", conversionRate));
        indicators.put("customerSatisfaction", "4.5/5.0"); // Mock
        // Occupancy: (Rented / Total Rentable Stock).
        long totalRentable = inventory.stream()
                .filter(p -> "alquiler".equalsIgnoreCase(p.getTipo()) || "ambos".equalsIgnoreCase(p.getTipo()))
                .mapToLong(p -> p.getStock() != null ? p.getStock() : 0).sum();
        long currentlyRented = rentals.stream().filter(a -> "activo".equalsIgnoreCase(a.getEstado())).count(); // Approximation
        double occupancyRate = totalRentable > 0 ? ((double) currentlyRented / totalRentable) * 100 : 0;
        indicators.put("occupancyRate", String.format("%.2f", occupancyRate));

        // --- Indicador de Gestión (Rotación de Inventario) ---
        // Turnover = Sales / Avg Inventory. Using Sales Count / Total Stock count as
        // proxy
        long totalStock = inventory.stream().mapToLong(p -> p.getStock() != null ? p.getStock() : 0).sum();
        double inventoryTurnover = totalStock > 0 ? (double) sales.size() / totalStock : 0;
        indicators.put("inventoryTurnover", String.format("%.2f", inventoryTurnover));

        // --- Indicador de Pérdidas ---
        // Loss % = (Lost Value / Initial Value). Mocking lost value as 1% of stock
        // value
        BigDecimal stockValue = inventory.stream()
                .map(p -> (p.getPrecioVenta() != null ? p.getPrecioVenta() : BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(p.getStock() != null ? p.getStock() : 0)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal estimatedLoss = stockValue.multiply(BigDecimal.valueOf(0.01));
        double lossPercentage = stockValue.compareTo(BigDecimal.ZERO) > 0
                ? (estimatedLoss.divide(stockValue, 4, RoundingMode.HALF_UP).doubleValue()) * 100
                : 0;
        indicators.put("lossPercentage", String.format("%.2f", lossPercentage));

        // --- Indicador de tiempo de reposición ---
        // Mocking as 3 days
        indicators.put("replenishmentTime", "3 días");

        // --- Indicador de Tasa de Crecimiento en Ventas ---
        // Growth = (Current - Previous) / Previous * 100
        // Calculating previous period sales
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate prevStart = startDate.minusDays(days + 1);
        LocalDate prevEnd = startDate.minusDays(1);
        List<Compra> prevSales = getSalesByDateRange(prevStart, prevEnd);
        BigDecimal prevTotalSales = prevSales.stream().map(c -> c.getTotal() != null ? c.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double growthRate = 0;
        if (prevTotalSales.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = (totalSales.subtract(prevTotalSales).divide(prevTotalSales, 4, RoundingMode.HALF_UP)
                    .doubleValue()) * 100;
        } else if (totalSales.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = 100; // 100% growth if previous was 0
        }
        indicators.put("salesGrowthRate", String.format("%.2f", growthRate));

        // --- Indicadores financieros ---
        indicators.put("totalRevenue", totalSales.add(totalRentals));
        // Margin: Assuming 30% margin
        indicators.put("profitMargin", "30%");
        // Delinquency: Late rentals / Total active rentals
        long lateRentals = rentals.stream()
                .filter(a -> "atrasado".equalsIgnoreCase(a.getEstado())
                        || (a.getFechaFin().isBefore(LocalDate.now()) && "activo".equalsIgnoreCase(a.getEstado())))
                .count();
        double delinquencyRate = rentals.size() > 0 ? ((double) lateRentals / rentals.size()) * 100 : 0;
        indicators.put("delinquencyRate", String.format("%.2f", delinquencyRate));

        // --- Indicadores de clientes/usuarios ---
        indicators.put("newVsRecurring", "80% / 20%"); // Mock
        // Avg Rental Duration
        double avgDuration = rentals.stream().mapToInt(a -> a.getDias() != null ? a.getDias() : 0).average().orElse(0);
        indicators.put("avgRentalDuration", String.format("%.1f días", avgDuration));

        // --- Indicadores de procesos ---
        indicators.put("responseTime", "2 horas"); // Mock
        indicators.put("billingEfficiency", "98%"); // Mock
        indicators.put("scheduleCompliance", String.format("%.2f%%", 100 - delinquencyRate));

        // --- Indicadores de mercado ---
        indicators.put("marketShare", "15%"); // Mock
        indicators.put("unsatisfiedDemand", "5%"); // Mock

        // --- Indicadores de calidad ---
        indicators.put("returnRate", "2%"); // Mock
        indicators.put("maintenanceCompliance", "95%"); // Mock

        return indicators;
    }

    // 4. Statistics (Max, Min, Avg)
    public Map<String, Object> getStatistics(LocalDate startDate, LocalDate endDate) {
        List<Compra> sales = getSalesByDateRange(startDate, endDate);
        List<Alquiler> rentals = getRentalsByDateRange(startDate, endDate);

        Map<String, Object> stats = new HashMap<>();

        // Sales Stats
        stats.put("sales", calculateStats(
                sales.stream().map(c -> c.getTotal() != null ? c.getTotal() : BigDecimal.ZERO).toList()));

        // Rental Stats
        stats.put("rentals", calculateStats(
                rentals.stream().map(a -> a.getTotal() != null ? a.getTotal() : BigDecimal.ZERO).toList()));

        return stats;
    }

    private Map<String, BigDecimal> calculateStats(List<BigDecimal> amounts) {
        Map<String, BigDecimal> result = new HashMap<>();
        if (amounts.isEmpty()) {
            result.put("max", BigDecimal.ZERO);
            result.put("min", BigDecimal.ZERO);
            result.put("avg", BigDecimal.ZERO);
            return result;
        }

        BigDecimal max = amounts.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal min = amounts.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal total = amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = total.divide(BigDecimal.valueOf(amounts.size()), 2, RoundingMode.HALF_UP);

        result.put("max", max);
        result.put("min", min);
        result.put("avg", avg);
        return result;
    }

    // 5. Deleted Records
    public List<Producto> getDeletedProducts() {
        return productoRepository.findByEstado("no disponible");
    }

    // 6. Financials (Income, Utility, Stock Value)
    public Map<String, Object> getFinancials(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> financials = new HashMap<>();

        // Income for the range
        Map<String, Object> indicators = getIndicators(startDate, endDate);
        BigDecimal totalIncome = ((BigDecimal) indicators.get("totalSalesAmount"))
                .add((BigDecimal) indicators.get("totalRentalsAmount"));

        financials.put("dailyIncome", totalIncome);
        // Utility logic would go here if we had cost price. For now, using income.
        financials.put("dailyUtility", totalIncome);

        // Stock Value
        List<Producto> products = productoRepository.findByEstado("disponible");
        BigDecimal stockValue = products.stream()
                .map(p -> {
                    BigDecimal price = p.getPrecioVenta() != null ? p.getPrecioVenta() : BigDecimal.ZERO;
                    int stock = p.getStock() != null ? p.getStock() : 0;
                    return price.multiply(BigDecimal.valueOf(stock));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        financials.put("stockValue", stockValue);

        return financials;
    }

    // 7. Unified Transactions (Sales + Rentals)
    public List<Map<String, Object>> getAllTransactions(LocalDate startDate, LocalDate endDate) {
        List<Compra> sales = getSalesByDateRange(startDate, endDate);
        List<Alquiler> rentals = getRentalsByDateRange(startDate, endDate);

        List<Map<String, Object>> transactions = new java.util.ArrayList<>();

        for (Compra c : sales) {
            Map<String, Object> t = new HashMap<>();
            t.put("id", c.getId());
            t.put("fecha", c.getFecha());
            t.put("usuario", c.getUsuario());
            t.put("producto", c.getProducto());
            t.put("cantidad", c.getCantidad());
            t.put("total", c.getTotal());
            t.put("tipo", "Venta");
            transactions.add(t);
        }

        for (Alquiler a : rentals) {
            Map<String, Object> t = new HashMap<>();
            t.put("id", a.getId());
            t.put("fecha", a.getFechaRegistro());
            t.put("usuario", a.getUsuario());
            t.put("producto", a.getProducto());
            t.put("cantidad", a.getCantidad());
            t.put("total", a.getTotal());
            t.put("tipo", "Alquiler");
            transactions.add(t);
        }

        transactions.sort((t1, t2) -> ((LocalDateTime) t2.get("fecha")).compareTo((LocalDateTime) t1.get("fecha")));

        return transactions;
    }
}
