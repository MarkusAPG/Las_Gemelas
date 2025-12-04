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

    @Autowired
    private com.lasgemelas.repository.ReporteRepository reporteRepository;

    // Helper to check admin role
    private boolean isAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        return usuario != null && "admin".equals(usuario.getRol());
    }

    private void logAction(String action, String entity, String description, HttpSession session) {
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin != null) {
            com.lasgemelas.model.Reporte reporte = new com.lasgemelas.model.Reporte(action, entity, description, admin);
            reporteRepository.save(reporte);
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/";
        return "admin/dashboard";
    }

    @GetMapping
    public String adminIndex(HttpSession session) {
        return "redirect:/admin/dashboard";
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
        logAction(producto.getId() == null ? "CREATE" : "UPDATE", "PRODUCTO",
                "Guardó el producto: " + producto.getNombre(), session);
        return "redirect:/admin/products";
    }

    @PostMapping("/products/updateStock")
    public String updateStock(@RequestParam Long id, @RequestParam int stock, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/";

        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto != null) {
            int oldStock = producto.getStock();
            producto.setStock(stock);
            productoRepository.save(producto);
            logAction("UPDATE", "PRODUCTO",
                    "Actualizó stock de " + producto.getNombre() + " de " + oldStock + " a " + stock, session);
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/toggle/{id}")
    public String toggleProductStatus(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/";

        productoRepository.findById(id).ifPresent(producto -> {
            String oldStatus = producto.getEstado();
            String newStatus = "disponible".equals(oldStatus) ? "no disponible" : "disponible";
            producto.setEstado(newStatus);
            productoRepository.save(producto);

            logAction("UPDATE", "PRODUCTO",
                    "Cambió estado de " + producto.getNombre() + " de " + oldStatus + " a " + newStatus, session);
        });

        return "redirect:/admin/products";
    }

    @Autowired
    private com.lasgemelas.repository.CompraRepository compraRepository;

    @Autowired
    private com.lasgemelas.repository.AlquilerRepository alquilerRepository;

    @Autowired
    private com.lasgemelas.repository.TicketDetalleRepository ticketDetalleRepository;

    @GetMapping("/products/delete/{id}")
    @org.springframework.transaction.annotation.Transactional
    public String deleteProduct(@PathVariable Long id, HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (!isAdmin(session))
            return "redirect:/";

        try {
            productoRepository.findById(id).ifPresent(producto -> {
                // Cascade delete related records
                ticketDetalleRepository.deleteByProductoId(id);
                compraRepository.deleteByProductoId(id);
                alquilerRepository.deleteByProductoId(id);

                // Delete the product
                productoRepository.delete(producto);
                logAction("DELETE", "PRODUCTO",
                        "Eliminó el producto: " + producto.getNombre() + " y sus registros relacionados", session);
                redirectAttributes.addFlashAttribute("success",
                        "Producto y todos sus registros relacionados eliminados correctamente.");
            });
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Ocurrió un error al eliminar el producto: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }

    // --- User Management ---

    @GetMapping("/users")
    public String listUsers(@RequestParam(required = false) String role, HttpSession session, Model model) {
        if (!isAdmin(session))
            return "redirect:/";

        if (role != null && !role.isEmpty()) {
            // Filter by role if provided
            model.addAttribute("usuarios", usuarioRepository.findAll().stream()
                    .filter(u -> role.equals(u.getRol()))
                    .toList());
            model.addAttribute("currentRole", role);
        } else {
            model.addAttribute("usuarios", usuarioRepository.findAll());
        }

        model.addAttribute("usuario", new Usuario()); // Form backing object
        return "admin/users";
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute Usuario usuario, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/";
        usuarioRepository.save(usuario);
        logAction("CREATE", "USUARIO", "Creó el usuario: " + usuario.getNombre() + " (" + usuario.getRol() + ")",
                session);
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
