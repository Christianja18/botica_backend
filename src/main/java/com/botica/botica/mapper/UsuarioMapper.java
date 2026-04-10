package com.botica.botica.mapper;

import com.botica.botica.dto.UsuarioDTO;
import com.botica.botica.entity.Rol;
import com.botica.botica.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class UsuarioMapper {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        UsuarioDTO dto = UsuarioDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .activo(usuario.getActivo())
                .build();

        if (usuario.getRol() != null) {
            dto.setIdRol(usuario.getRol().getIdRol());
            dto.setRol(toRolDTO(usuario.getRol()));
        }

        if (usuario.getFechaCreacion() != null) {
            dto.setFechaCreacion(usuario.getFechaCreacion().format(dateFormatter));
        }

        return dto;
    }

    public Usuario toEntity(UsuarioDTO dto) {
        if (dto == null) {
            return null;
        }
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(dto.getIdUsuario());
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setEmail(dto.getEmail());
        usuario.setPasswordHash(dto.getPasswordHash());
        usuario.setActivo(dto.getActivo());

        if (usuario.getFechaCreacion() == null) {
            usuario.setFechaCreacion(LocalDateTime.now());
        }

        return usuario;
    }

    public Usuario updateEntity(UsuarioDTO dto, Usuario usuario) {
        if (dto == null) {
            return usuario;
        }
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setEmail(dto.getEmail());
        usuario.setActivo(dto.getActivo());

        if (dto.getPasswordHash() != null && !dto.getPasswordHash().isBlank()) {
            usuario.setPasswordHash(dto.getPasswordHash());
        }

        return usuario;
    }

    private UsuarioDTO.RolResumenDTO toRolDTO(Rol rol) {
        return UsuarioDTO.RolResumenDTO.builder()
                .idRol(rol.getIdRol())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .activo(rol.getActivo())
                .puedeVender(rol.getPuedeVender())
                .puedeAdministrarInventario(rol.getPuedeAdministrarInventario())
                .puedeVerReportes(rol.getPuedeVerReportes())
                .puedeAdministrarUsuarios(rol.getPuedeAdministrarUsuarios())
                .build();
    }
}
