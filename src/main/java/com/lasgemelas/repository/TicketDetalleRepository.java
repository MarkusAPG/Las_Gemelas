package com.lasgemelas.repository;

import com.lasgemelas.model.TicketDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketDetalleRepository extends JpaRepository<TicketDetalle, Integer> {
    void deleteByProductoId(Long productoId);
}
