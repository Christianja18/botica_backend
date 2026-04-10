package com.botica.botica.repository;

import com.botica.botica.entity.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Override
    @EntityGraph(attributePaths = {
            "cliente",
            "usuario",
            "detalles",
            "detalles.producto"
    })
    List<Pedido> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "cliente",
            "usuario",
            "detalles",
            "detalles.producto"
    })
    java.util.Optional<Pedido> findById(Integer id);

    @EntityGraph(attributePaths = {
            "cliente",
            "usuario",
            "detalles",
            "detalles.producto"
    })
    List<Pedido> findByEstado(Pedido.EstadoPedido estado);

    List<Pedido> findByFechaPedidoBetween(LocalDateTime start, LocalDateTime end);

    List<Pedido> findByFechaPedidoBetweenAndEstado(LocalDateTime start, LocalDateTime end, Pedido.EstadoPedido estado);
}
