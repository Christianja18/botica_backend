package com.botica.botica.repository;

import com.botica.botica.entity.Boleta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Integer> {

    @Override
    @EntityGraph(attributePaths = {
            "pedido",
            "pedido.cliente",
            "pedido.usuario",
            "pedido.detalles",
            "pedido.detalles.producto"
    })
    List<Boleta> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "pedido",
            "pedido.cliente",
            "pedido.usuario",
            "pedido.detalles",
            "pedido.detalles.producto"
    })
    Page<Boleta> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {
            "pedido",
            "pedido.cliente",
            "pedido.usuario",
            "pedido.detalles",
            "pedido.detalles.producto"
    })
    Optional<Boleta> findById(Integer id);

    Optional<Boleta> findByNumeroBoleta(String numeroBoleta);
}
