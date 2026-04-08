package com.botica.botica.controller;

import com.botica.botica.dto.ClienteDTO;
import com.botica.botica.entity.Cliente;
import com.botica.botica.mapper.ClienteMapper;
import com.botica.botica.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Operaciones CRUD para clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteMapper clienteMapper;

    @GetMapping
    @Operation(summary = "Obtener todos los clientes", description = "Retorna una lista de todos los clientes registrados")
    public ResponseEntity<List<ClienteDTO>> getAllClientes() {
        List<Cliente> clientes = clienteService.findAll();
        List<ClienteDTO> dtos = clientes.stream()
                .map(clienteMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Retorna los detalles de un cliente especifico")
    public ResponseEntity<ClienteDTO> getClienteById(@PathVariable Integer id) {
        Cliente cliente = clienteService.findById(id);
        return ResponseEntity.ok(clienteMapper.toDTO(cliente));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo cliente", description = "Crea un nuevo cliente en el sistema")
    public ResponseEntity<ClienteDTO> createCliente(@Valid @RequestBody ClienteDTO clienteDTO) {
        Cliente cliente = clienteMapper.toEntity(clienteDTO);
        Cliente saved = clienteService.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente")
    public ResponseEntity<ClienteDTO> updateCliente(@PathVariable Integer id, @Valid @RequestBody ClienteDTO clienteDTO) {
        Cliente existing = clienteService.findById(id);
        Cliente updated = clienteMapper.updateEntity(clienteDTO, existing);
        Cliente saved = clienteService.save(updated);
        return ResponseEntity.ok(clienteMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente", description = "Elimina un cliente registrado por su identificador")
    public ResponseEntity<Void> deleteCliente(@PathVariable Integer id) {
        clienteService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
