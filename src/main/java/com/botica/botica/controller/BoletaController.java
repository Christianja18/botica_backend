package com.botica.botica.controller;

import com.botica.botica.dto.BoletaDTO;
import com.botica.botica.entity.Boleta;
import com.botica.botica.mapper.BoletaMapper;
import com.botica.botica.service.BoletaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boletas")
@Tag(name = "Boletas", description = "Operaciones CRUD para boletas")
@RequiredArgsConstructor
public class BoletaController {

    private final BoletaService boletaService;
    private final BoletaMapper boletaMapper;

    @GetMapping
    public ResponseEntity<List<BoletaDTO>> getAllBoletas() {
        return ResponseEntity.ok(boletaService.findAll().stream()
                .map(boletaMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoletaDTO> getBoletaById(@PathVariable Integer id) {
        return ResponseEntity.ok(boletaMapper.toDTO(boletaService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<BoletaDTO> createBoleta(@Valid @RequestBody BoletaDTO boletaDTO) {
        Boleta saved = boletaService.saveFromDto(boletaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(boletaMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoletaDTO> updateBoleta(@PathVariable Integer id, @Valid @RequestBody BoletaDTO boletaDTO) {
        boletaDTO.setIdBoleta(id);
        Boleta saved = boletaService.saveFromDto(boletaDTO);
        return ResponseEntity.ok(boletaMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoleta(@PathVariable Integer id) {
        boletaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
