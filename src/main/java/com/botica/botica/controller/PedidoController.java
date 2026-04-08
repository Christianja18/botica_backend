package com.botica.botica.controller;

import com.botica.botica.dto.PedidoDTO;
import com.botica.botica.entity.Pedido;
import com.botica.botica.mapper.PedidoMapper;
import com.botica.botica.service.PedidoService;
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
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Operaciones CRUD para pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final PedidoMapper pedidoMapper;

    @GetMapping
    @Operation(summary = "Obtener todos los pedidos", description = "Retorna una lista de todos los pedidos registrados")
    public ResponseEntity<List<PedidoDTO>> getAllPedidos() {
        return ResponseEntity.ok(pedidoService.findAll().stream()
                .map(pedidoMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pedido por ID", description = "Retorna los detalles de un pedido especifico")
    public ResponseEntity<PedidoDTO> getPedidoById(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoMapper.toDTO(pedidoService.findById(id)));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener pedidos por estado", description = "Retorna una lista de pedidos filtrados por su estado")
    public ResponseEntity<List<PedidoDTO>> getPedidosByEstado(@PathVariable Pedido.EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.findByEstado(estado).stream()
                .map(pedidoMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo pedido", description = "Crea un nuevo pedido con su informacion principal y sus detalles")
    public ResponseEntity<PedidoDTO> createPedido(@Valid @RequestBody PedidoDTO pedidoDTO) {
        Pedido saved = pedidoService.saveFromDto(pedidoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar pedido", description = "Actualiza los datos de un pedido existente")
    public ResponseEntity<PedidoDTO> updatePedido(@PathVariable Integer id, @Valid @RequestBody PedidoDTO pedidoDTO) {
        pedidoDTO.setIdPedido(id);
        Pedido saved = pedidoService.saveFromDto(pedidoDTO);
        return ResponseEntity.ok(pedidoMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pedido", description = "Elimina un pedido registrado por su identificador")
    public ResponseEntity<Void> deletePedido(@PathVariable Integer id) {
        pedidoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
