package com.lasgemelas.controller;

import com.lasgemelas.model.Alquiler;
import com.lasgemelas.model.Usuario;
import com.lasgemelas.repository.AlquilerRepository;
import com.lasgemelas.repository.CompraRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/tecnico")
public class TechnicianController {

    @Autowired
    private AlquilerRepository alquilerRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private com.lasgemelas.repository.TicketRepository ticketRepository;

    private boolean isTechnician(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        return usuario != null && "tecnico".equals(usuario.getRol());
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        if (!isTechnician(session)) {
            return "redirect:/";
        }
        model.addAttribute("alquileres", alquilerRepository.findAll());
        model.addAttribute("compras", compraRepository.findAll());
        model.addAttribute("tickets", ticketRepository.findAllByOrderByFechaDesc());
        return "tecnico";
    }

    @GetMapping("/tickets/{id}")
    public String viewTicket(@PathVariable Integer id, Model model, HttpSession session) {
        if (!isTechnician(session))
            return "redirect:/";
        ticketRepository.findById(id).ifPresent(ticket -> model.addAttribute("ticket", ticket));
        return "ticket";
    }

    @GetMapping("/return/{id}")
    public String returnRental(@PathVariable Long id, HttpSession session) {
        if (!isTechnician(session)) {
            return "redirect:/";
        }
        Optional<Alquiler> alquilerOpt = alquilerRepository.findById(id);
        if (alquilerOpt.isPresent()) {
            Alquiler alquiler = alquilerOpt.get();
            alquiler.setEstado("devuelto");
            alquilerRepository.save(alquiler);
        }
        return "redirect:/tecnico";
    }
}
