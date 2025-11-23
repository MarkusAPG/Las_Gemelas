package com.lasgemelas.repository;

import com.lasgemelas.model.Alquiler;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlquilerRepository extends JpaRepository<Alquiler, Long> {
    List<Alquiler> findByEstado(String estado);
}
