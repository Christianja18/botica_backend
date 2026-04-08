package com.botica.botica.mapper;

import com.botica.botica.dto.ClienteDTO;
import com.botica.botica.entity.Cliente;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ClienteMapper {

    private final ModelMapper modelMapper;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ClienteDTO toDTO(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        ClienteDTO dto = modelMapper.map(cliente, ClienteDTO.class);
        if (cliente.getFechaCreacion() != null) {
            dto.setFechaCreacion(cliente.getFechaCreacion().format(dateFormatter));
        }
        return dto;
    }

    public Cliente toEntity(ClienteDTO dto) {
        if (dto == null) {
            return null;
        }
        Cliente cliente = modelMapper.map(dto, Cliente.class);
        if (cliente.getFechaCreacion() == null) {
            cliente.setFechaCreacion(LocalDateTime.now());
        }
        return cliente;
    }

    public Cliente updateEntity(ClienteDTO dto, Cliente cliente) {
        if (dto == null) {
            return cliente;
        }
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setDni(dto.getDni());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        return cliente;
    }
}
