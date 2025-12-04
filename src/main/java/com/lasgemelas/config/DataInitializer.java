package com.lasgemelas.config;

import com.lasgemelas.model.Usuario;
import com.lasgemelas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin exists
        if (usuarioRepository.findByCorreo("admin@tienda.com") == null) {
            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setApellido("Principal");
            admin.setCorreo("admin@tienda.com");
            admin.setContrasena("12345"); // In a real app, use BCrypt
            admin.setTelefono("999888777");
            admin.setDireccion("Oficina Central");
            admin.setRol("admin");

            usuarioRepository.save(admin);
            System.out.println("Admin user created: admin@tienda.com / 12345");
        }
    }
}
