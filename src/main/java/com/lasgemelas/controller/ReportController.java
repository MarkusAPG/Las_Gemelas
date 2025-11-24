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
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        if (date == null) {
            date = LocalDate.now();
        }

        model.addAttribute("selectedDate", date);

        // 1. & 2. Sales, Rentals, Inventory
        model.addAttribute("dailySales", reportService.getDailySales(date));
        model.addAttribute("dailyRentals", reportService.getDailyRentals(date));
        model.addAttribute("inventory", reportService.getInventory());

        // 3. Indicators
        model.addAttribute("indicators", reportService.getIndicators(date));

        // 4. Statistics
        model.addAttribute("statistics", reportService.getStatistics(date));

        // 5. Deleted Records
        model.addAttribute("deletedProducts", reportService.getDeletedProducts());

        // 6. Financials
        model.addAttribute("financials", reportService.getFinancials(date));

        return "admin/reports";
    }
}
