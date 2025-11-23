package com.lasgemelas.controller;

import com.lasgemelas.model.CartItem;
import com.lasgemelas.model.Producto;
import com.lasgemelas.repository.ProductoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private com.lasgemelas.repository.TicketRepository ticketRepository;

    @Autowired
    private com.lasgemelas.repository.TicketDetalleRepository ticketDetalleRepository;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        List<CartItem> cart = getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("total", calculateTotal(cart));
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, @RequestParam String type, HttpSession session) {
        List<CartItem> cart = getCart(session);
        Optional<Producto> productOpt = productoRepository.findById(productId);

        if (productOpt.isPresent()) {
            Producto product = productOpt.get();
            boolean exists = false;
            for (CartItem item : cart) {
                if (item.getProducto().getId().equals(productId) && item.getTipo().equals(type)) {
                    item.setCantidad(item.getCantidad() + 1);
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                cart.add(new CartItem(product, 1, type));
            }
            session.setAttribute("cartSize", cart.stream().mapToInt(CartItem::getCantidad).sum());
        }

        return "redirect:/";
    }

    @GetMapping("/remove/{index}")
    public String removeFromCart(@PathVariable int index, HttpSession session) {
        List<CartItem> cart = getCart(session);
        if (index >= 0 && index < cart.size()) {
            cart.remove(index);
        }
        session.setAttribute("cartSize", cart.stream().mapToInt(CartItem::getCantidad).sum());
        return "redirect:/cart";
    }

    @GetMapping("/clear")
    public String clearCart(HttpSession session) {
        session.removeAttribute("cart");
        session.removeAttribute("cartSize");
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session) {
        com.lasgemelas.model.Usuario usuario = (com.lasgemelas.model.Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        List<CartItem> cart = getCart(session);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }

        // Create Ticket
        com.lasgemelas.model.Ticket ticket = new com.lasgemelas.model.Ticket();
        ticket.setUsuario(usuario);
        ticket.setTotal(calculateTotal(cart));
        ticket = ticketRepository.save(ticket);

        // Create Details
        for (CartItem item : cart) {
            com.lasgemelas.model.TicketDetalle detalle = new com.lasgemelas.model.TicketDetalle();
            detalle.setTicket(ticket);
            detalle.setProducto(item.getProducto());
            detalle.setCantidad(item.getCantidad());
            detalle.setTipo(item.getTipo());

            BigDecimal precio = "alquiler".equals(item.getTipo()) ? item.getProducto().getPrecioAlquiler()
                    : item.getProducto().getPrecioVenta();
            detalle.setPrecioUnitario(precio);
            detalle.setSubtotal(item.getSubtotal());

            ticketDetalleRepository.save(detalle);
        }

        // Clear Cart
        clearCart(session);

        return "redirect:/cart/ticket/" + ticket.getId();
    }

    @GetMapping("/ticket/{id}")
    public String viewTicket(@PathVariable Integer id, Model model, HttpSession session) {
        com.lasgemelas.model.Usuario usuario = (com.lasgemelas.model.Usuario) session.getAttribute("usuario");
        if (usuario == null)
            return "redirect:/login";

        Optional<com.lasgemelas.model.Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            com.lasgemelas.model.Ticket ticket = ticketOpt.get();
            // Security check: only allow viewing own tickets unless admin
            if (!ticket.getUsuario().getId().equals(usuario.getId()) && !"admin".equals(usuario.getRol())) {
                return "redirect:/";
            }
            model.addAttribute("ticket", ticket);
            return "ticket";
        }
        return "redirect:/";
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    private BigDecimal calculateTotal(List<CartItem> cart) {
        return cart.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
