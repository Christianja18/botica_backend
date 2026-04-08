package com.botica.botica.mapper;

import com.botica.botica.dto.ProveedorDTO;
import com.botica.botica.entity.Proveedor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ProveedorMapper {

    private final ModelMapper modelMapper;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ProveedorDTO toDTO(Proveedor proveedor) {
        if (proveedor == null) {
            return null;
        }
        ProveedorDTO dto = modelMapper.map(proveedor, ProveedorDTO.class);
        if (proveedor.getFechaCreacion() != null) {
            dto.setFechaCreacion(proveedor.getFechaCreacion().format(dateFormatter));
        }
        return dto;
    }

    public Proveedor toEntity(ProveedorDTO dto) {
        if (dto == null) {
            return null;
        }
        Proveedor proveedor = modelMapper.map(dto, Proveedor.class);
        if (proveedor.getFechaCreacion() == null) {
            proveedor.setFechaCreacion(LocalDateTime.now());
        }
        return proveedor;
    }

    public Proveedor updateEntity(ProveedorDTO dto, Proveedor proveedor) {
        if (dto == null) {
            return proveedor;
        }
        proveedor.setNombre(dto.getNombre());
        proveedor.setRuc(dto.getRuc());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setEmail(dto.getEmail());
        proveedor.setDireccion(dto.getDireccion());
        return proveedor;
    }
}
