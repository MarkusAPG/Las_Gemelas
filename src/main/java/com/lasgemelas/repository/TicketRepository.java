package com.lasgemelas.repository;

import com.lasgemelas.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByUsuarioIdOrderByFechaDesc(Integer usuarioId);

    List<Ticket> findAllByOrderByFechaDesc();
}
