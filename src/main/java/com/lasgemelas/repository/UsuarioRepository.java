package com.lasgemelas.repository;

import com.lasgemelas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByCorreo(String correo);

    Usuario findByCorreoAndContrasena(String correo, String contrasena);
}
