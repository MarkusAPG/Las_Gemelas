package com.lasgemelas.controller;

import com.lasgemelas.model.Producto;
import com.lasgemelas.repository.ProductoRepository;
import com.lasgemelas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tecnico")
public class AdminController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("nuevoProducto", new Producto());
        return "technician";
    }

    @PostMapping("/producto/guardar")
    public String guardarProducto(@ModelAttribute Producto producto) {
        productoRepository.save(producto);
        return "redirect:/tecnico";
    }

    @GetMapping("/producto/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {
        productoRepository.deleteById(id);
        return "redirect:/tecnico";
    }

    @GetMapping("/usuario/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return "redirect:/tecnico";
    }
}
