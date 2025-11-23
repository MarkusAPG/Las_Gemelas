package com.lasgemelas.controller;

import com.lasgemelas.model.Usuario;
import com.lasgemelas.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String loginProcess(@RequestParam String correo, @RequestParam String contrasena, HttpSession session, Model model) {
        Usuario usuario = usuarioRepository.findByCorreoAndContrasena(correo, contrasena);
        if (usuario != null) {
            session.setAttribute("usuario", usuario);
            if ("admin".equals(usuario.getRol()) || "tecnico".equals(usuario.getRol())) {
                return "redirect:/tecnico";
            }
            return "redirect:/";
        } else {
            model.addAttribute("error", "Credenciales inválidas");
            return "login";
        }
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "register";
    }

    @PostMapping("/register")
    public String registerProcess(@ModelAttribute Usuario usuario, Model model) {
        // Check if email already exists
        Usuario existingUser = usuarioRepository.findByCorreo(usuario.getCorreo());
        if (existingUser != null) {
            model.addAttribute("error", "El correo electrónico ya está registrado");
            model.addAttribute("usuario", usuario);
            return "register";
        }
        
        usuario.setRol("cliente");
        usuarioRepository.save(usuario);
        return "redirect:/login?registered=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
