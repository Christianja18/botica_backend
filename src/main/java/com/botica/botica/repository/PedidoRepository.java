package com.botica.botica.repository;

import com.botica.botica.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByEstado(Pedido.EstadoPedido estado);

    List<Pedido> findByFechaPedidoBetween(LocalDateTime start, LocalDateTime end);
}