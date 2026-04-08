package com.botica.botica.repository;

import com.botica.botica.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {

    List<DetallePedido> findByPedidoIdPedido(Integer pedidoId);
}
