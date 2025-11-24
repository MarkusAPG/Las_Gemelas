package com.lasgemelas.repository;

import com.lasgemelas.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByFechaBetween(LocalDateTime start, LocalDateTime end);
}
