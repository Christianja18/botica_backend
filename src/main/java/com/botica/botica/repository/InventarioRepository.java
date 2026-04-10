package com.botica.botica.repository;

import com.botica.botica.entity.Inventario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Integer> {

    @Override
    @EntityGraph(attributePaths = {"producto"})
    List<Inventario> findAll();

    @Override
    @EntityGraph(attributePaths = {"producto"})
    Page<Inventario> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"producto"})
    Optional<Inventario> findById(Integer id);

    @EntityGraph(attributePaths = {"producto"})
    Optional<Inventario> findByProductoIdProducto(Integer productoId);
}
