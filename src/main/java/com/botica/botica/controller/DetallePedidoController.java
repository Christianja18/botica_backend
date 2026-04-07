package com.botica.botica.controller;

import com.botica.botica.entity.DetallePedido;
import com.botica.botica.service.DetallePedidoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalles-pedido")
@Tag(name = "DetallePedido", description = "Operaciones CRUD para detalles de pedido")
@RequiredArgsConstructor
public class DetallePedidoController {

    private final DetallePedidoService detallePedidoService;

    @GetMapping
    public ResponseEntity<List<DetallePedido>> getAllDetallesPedido() {
        return ResponseEntity.ok(detallePedidoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetallePedido> getDetallePedidoById(@PathVariable Integer id) {
        return ResponseEntity.ok(detallePedidoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DetallePedido> createDetallePedido(@Valid @RequestBody DetallePedido detallePedido) {
        return ResponseEntity.ok(detallePedidoService.save(detallePedido));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetallePedido> updateDetallePedido(@PathVariable Integer id, @Valid @RequestBody DetallePedido detallePedido) {
        detallePedido.setIdDetalle(id);
        return ResponseEntity.ok(detallePedidoService.save(detallePedido));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDetallePedido(@PathVariable Integer id) {
        detallePedidoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
