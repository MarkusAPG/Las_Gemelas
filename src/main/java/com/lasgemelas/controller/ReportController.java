package com.lasgemelas.controller;

import com.lasgemelas.model.Usuario;
import com.lasgemelas.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    private boolean isAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        return usuario != null && "admin".equals(usuario.getRol());
    }

    @GetMapping
    public String viewReports(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        // Default to today if no date provided
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate; // Default to single day if end date not provided
        }

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // 1. & 2. Sales and Rentals (Range)
        model.addAttribute("sales", reportService.getSalesByDateRange(startDate, endDate));
        model.addAttribute("rentals", reportService.getRentalsByDateRange(startDate, endDate));

        // Inventory is static
        model.addAttribute("inventory", reportService.getInventory());

        // 3. Indicators
        model.addAttribute("indicators", reportService.getIndicators(startDate, endDate));

        // 4. Statistics
        model.addAttribute("statistics", reportService.getStatistics(startDate, endDate));

        // 5. Deleted Records
        model.addAttribute("deletedProducts", reportService.getDeletedProducts());

        // 6. Financials
        model.addAttribute("financials", reportService.getFinancials(startDate, endDate));

        return "admin/reports";
    }
}
