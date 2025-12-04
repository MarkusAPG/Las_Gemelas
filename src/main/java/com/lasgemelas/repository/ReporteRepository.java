package com.lasgemelas.repository;

import com.lasgemelas.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    List<Reporte> findByFechaBetweenOrderByFechaDesc(LocalDateTime start, LocalDateTime end);
}
