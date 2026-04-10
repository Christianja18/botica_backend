package com.botica.botica.repository;

import com.botica.botica.entity.Producto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    @Override
    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    List<Producto> findAll();

    @Override
    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    Page<Producto> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    Optional<Producto> findById(Integer id);

    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    List<Producto> findByCategoriaIdCategoria(Integer idCategoria);

    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    Optional<Producto> findByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarras(String codigoBarras);
}
