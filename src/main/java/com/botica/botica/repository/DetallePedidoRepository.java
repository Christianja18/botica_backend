package com.botica.botica.repository;

import com.botica.botica.entity.DetallePedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {

    @Override
    @EntityGraph(attributePaths = {"pedido", "pedido.cliente", "pedido.usuario", "producto"})
    List<DetallePedido> findAll();

    @Override
    @EntityGraph(attributePaths = {"pedido", "pedido.cliente", "pedido.usuario", "producto"})
    java.util.Optional<DetallePedido> findById(Integer id);

    @EntityGraph(attributePaths = {"pedido", "pedido.cliente", "pedido.usuario", "producto"})
    List<DetallePedido> findByPedidoIdPedido(Integer pedidoId);
}
