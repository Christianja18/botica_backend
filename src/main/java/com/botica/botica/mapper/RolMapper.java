package com.botica.botica.mapper;

import com.botica.botica.dto.RolDTO;
import com.botica.botica.entity.Rol;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class RolMapper {

    private final ModelMapper modelMapper;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public RolDTO toDTO(Rol rol) {
        if (rol == null) {
            return null;
        }
        RolDTO dto = RolDTO.builder()
                .idRol(rol.getIdRol())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .puedeVender(rol.getPuedeVender())
                .puedeAdministrarInventario(rol.getPuedeAdministrarInventario())
                .puedeVerReportes(rol.getPuedeVerReportes())
                .puedeAdministrarUsuarios(rol.getPuedeAdministrarUsuarios())
                .activo(rol.getActivo())
                .build();

        if (rol.getFechaCreacion() != null) {
            dto.setFechaCreacion(rol.getFechaCreacion().format(dateFormatter));
        }

        return dto;
    }

    public Rol toEntity(RolDTO dto) {
        if (dto == null) {
            return null;
        }
        Rol rol = new Rol();
        rol.setIdRol(dto.getIdRol());
        rol.setNombre(dto.getNombre());
        rol.setDescripcion(dto.getDescripcion());
        rol.setPuedeVender(dto.getPuedeVender());
        rol.setPuedeAdministrarInventario(dto.getPuedeAdministrarInventario());
        rol.setPuedeVerReportes(dto.getPuedeVerReportes());
        rol.setPuedeAdministrarUsuarios(dto.getPuedeAdministrarUsuarios());
        rol.setActivo(dto.getActivo());

        if (rol.getFechaCreacion() == null) {
            rol.setFechaCreacion(LocalDateTime.now());
        }

        return rol;
    }

    public Rol updateEntity(RolDTO dto, Rol rol) {
        if (dto == null) {
            return rol;
        }
        rol.setNombre(dto.getNombre());
        rol.setDescripcion(dto.getDescripcion());
        rol.setPuedeVender(dto.getPuedeVender());
        rol.setPuedeAdministrarInventario(dto.getPuedeAdministrarInventario());
        rol.setPuedeVerReportes(dto.getPuedeVerReportes());
        rol.setPuedeAdministrarUsuarios(dto.getPuedeAdministrarUsuarios());
        rol.setActivo(dto.getActivo());

        return rol;
    }
}