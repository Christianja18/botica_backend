package com.botica.botica.repository;

import com.botica.botica.entity.Reporte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {

    @Override
    @EntityGraph(attributePaths = {"generadoPor"})
    List<Reporte> findAll();

    @Override
    @EntityGraph(attributePaths = {"generadoPor"})
    Page<Reporte> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"generadoPor"})
    java.util.Optional<Reporte> findById(Integer id);

    @EntityGraph(attributePaths = {"generadoPor"})
    List<Reporte> findByTipoReporte(Reporte.TipoReporte tipoReporte);

    @EntityGraph(attributePaths = {"generadoPor"})
    List<Reporte> findByFechaGeneracionBetween(LocalDateTime start, LocalDateTime end);
}
