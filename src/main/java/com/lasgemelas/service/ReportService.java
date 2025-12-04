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
}
