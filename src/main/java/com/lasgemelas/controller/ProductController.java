package com.lasgemelas.controller;

import com.lasgemelas.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductController {

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("productos", productoRepository.findByEstado("disponible"));
        return "index";
    }
}
