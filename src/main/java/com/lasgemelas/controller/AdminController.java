package com.lasgemelas.controller;

import com.lasgemelas.model.Producto;
import com.lasgemelas.model.Usuario;
import com.lasgemelas.repository.ProductoRepository;
import com.lasgemelas.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Helper to check admin role
    private boolean isAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        return usuario != null && "admin".equals(usuario.getRol());
    }

    @GetMapping
    public String dashboard(HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/";
        return "redirect:/admin/products"; // Default to products view
    }

    // --- Product Management ---

    @GetMapping("/products")
    public String listProducts(HttpSession session, Model model) {
        if (!isAdmin(session))
            return "redirect:/";
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("producto", new Producto()); // Form backing object
        return "admin/products";
    }

    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute Producto producto,
            @RequestParam("imageFile") MultipartFile imageFile,
            HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/";

        if (!imageFile.isEmpty()) {
            try {
                String fileName = imageFile.getOriginalFilename();
                // Save to absolute uploads directory
                String uploadDir = "C:/Users/vasqu/OneDrive/Desktop/Las Gemelas/uploads/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                try (InputStream inputStream = imageFile.getInputStream()) {
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                    producto.setImagen(fileName);
                }
            } catch (IOException e) {
                e.printStackTrace(); // Handle error appropriately
            }
        } else if (producto.getImagen() == null || producto.getImagen().isEmpty()) {
            producto.setImagen("placeholder.jpg");
        }

        productoRepository.save(producto);
        return "redirect:/admin/products";
    }

    @PostMapping("/products/updateStock")
    public String updateStock(@RequestParam Long id, @RequestParam int stock, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/";

        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto != null) {
            producto.setStock(stock);
            productoRepository.save(producto);
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/";

        productoRepository.findById(id).ifPresent(producto -> {
            producto.setEstado("no disponible");
            productoRepository.save(producto);
        });

        return "redirect:/admin/products";
    }

    // --- User Management ---

    @GetMapping("/users")
    public String listUsers(HttpSession session, Model model) {
        if (!isAdmin(session))
            return "redirect:/";
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("usuario", new Usuario()); // Form backing object
        return "admin/users";
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute Usuario usuario, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/";
        usuarioRepository.save(usuario);
        return "redirect:/admin/users";
    }

    @Autowired
    private com.lasgemelas.repository.TicketRepository ticketRepository;

    @GetMapping("/tickets")
    public String listTickets(Model model, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/";
        model.addAttribute("tickets", ticketRepository.findAllByOrderByFechaDesc());
        return "admin/tickets";
    }

    @GetMapping("/tickets/{id}")
    public String viewAdminTicket(@PathVariable Integer id, Model model, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/";
        ticketRepository.findById(id).ifPresent(ticket -> model.addAttribute("ticket", ticket));
        return "ticket"; // Re-use the ticket view
    }
}
