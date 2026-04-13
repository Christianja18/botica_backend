package com.botica.botica.repository;

import com.botica.botica.entity.Boleta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    Optional<Boleta> findById(Integer id);

    @Query(value = "select b.idBoleta from Boleta b",
            countQuery = "select count(b) from Boleta b")
    Page<Integer> findPageIds(Pageable pageable);

    @EntityGraph(attributePaths = {
            "pedido",
            "pedido.cliente",
            "pedido.usuario",
            "pedido.detalles",
            "pedido.detalles.producto"
    })
    List<Boleta> findByIdBoletaIn(List<Integer> ids);

    Optional<Boleta> findByNumeroBoleta(String numeroBoleta);
}
