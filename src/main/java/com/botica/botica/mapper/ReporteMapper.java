package com.botica.botica.mapper;

import com.botica.botica.dto.ReporteDTO;
import com.botica.botica.entity.Reporte;
import com.botica.botica.entity.Usuario;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ReporteMapper {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ReporteDTO toDTO(Reporte reporte) {
        if (reporte == null) {
            return null;
        }
        ReporteDTO dto = ReporteDTO.builder()
                .idReporte(reporte.getIdReporte())
                .tipoReporte(reporte.getTipoReporte().toString())
                .datos(reporte.getDatos())
                .build();

        if (reporte.getFechaGeneracion() != null) {
            dto.setFechaGeneracion(reporte.getFechaGeneracion().format(dateFormatter));
        }

        if (reporte.getFechaInicio() != null) {
            dto.setFechaInicio(reporte.getFechaInicio().format(dateFormatter));
        }

        if (reporte.getFechaFin() != null) {
            dto.setFechaFin(reporte.getFechaFin().format(dateFormatter));
        }

        if (reporte.getGeneradoPor() != null) {
            dto.setGeneradoPor(reporte.getGeneradoPor().getIdUsuario());
            dto.setUsuarioGenerador(toUsuarioDTO(reporte.getGeneradoPor()));
        }

        return dto;
    }

    public Reporte toEntity(ReporteDTO dto) {
        if (dto == null) {
            return null;
        }
        Reporte reporte = new Reporte();
        reporte.setIdReporte(dto.getIdReporte());

        if (dto.getTipoReporte() != null) {
            reporte.setTipoReporte(Reporte.TipoReporte.valueOf(dto.getTipoReporte()));
        }

        reporte.setDatos(dto.getDatos());

        if (reporte.getFechaGeneracion() == null) {
            reporte.setFechaGeneracion(LocalDateTime.now());
        }

        return reporte;
    }

    public Reporte updateEntity(ReporteDTO dto, Reporte reporte) {
        if (dto == null) {
            return reporte;
        }

        if (dto.getTipoReporte() != null) {
            reporte.setTipoReporte(Reporte.TipoReporte.valueOf(dto.getTipoReporte()));
        }

        reporte.setDatos(dto.getDatos());

        return reporte;
    }

    private ReporteDTO.UsuarioResumenDTO toUsuarioDTO(Usuario usuario) {
        return ReporteDTO.UsuarioResumenDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .build();
    }
}
