package com.botica.botica.repository;

import com.botica.botica.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {

    List<Reporte> findByTipoReporte(Reporte.TipoReporte tipoReporte);

    List<Reporte> findByFechaGeneracionBetween(LocalDateTime start, LocalDateTime end);
}