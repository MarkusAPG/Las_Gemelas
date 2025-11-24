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

    public List<Alquiler> getDailyRentals(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return alquilerRepository.findByFechaRegistroBetween(start, end);
    }

    // 1. Inventory Control (All products)
    public List<Producto> getInventory() {
        return productoRepository.findAll();
    }

    // 3. Indicators
    public Map<String, Object> getIndicators(LocalDate date) {
        List<Compra> sales = getDailySales(date);
        List<Alquiler> rentals = getDailyRentals(date);

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
    public Map<String, Object> getStatistics(LocalDate date) {
        List<Compra> sales = getDailySales(date);
        Map<String, Object> stats = new HashMap<>();

        if (sales.isEmpty()) {
            stats.put("maxSale", BigDecimal.ZERO);
            stats.put("minSale", BigDecimal.ZERO);
            stats.put("avgSale", BigDecimal.ZERO);
            return stats;
        }

        BigDecimal maxSale = sales.stream()
                .map(c -> c.getTotal() != null ? c.getTotal() : BigDecimal.ZERO)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal minSale = sales.stream()
                .map(c -> c.getTotal() != null ? c.getTotal() : BigDecimal.ZERO)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal totalSales = sales.stream()
                .map(c -> c.getTotal() != null ? c.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgSale = totalSales.divide(BigDecimal.valueOf(sales.size()), 2, RoundingMode.HALF_UP);

        stats.put("maxSale", maxSale);
        stats.put("minSale", minSale);
        stats.put("avgSale", avgSale);

        return stats;
    }

    // 5. Deleted Records
    public List<Producto> getDeletedProducts() {
        return productoRepository.findByEstado("no disponible");
    }

    // 6. Financials (Income, Utility, Stock Value)
    public Map<String, Object> getFinancials(LocalDate date) {
        Map<String, Object> financials = new HashMap<>();

        // Income for the day
        Map<String, Object> indicators = getIndicators(date);
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
