package com.botica.botica.service;

import com.botica.botica.dto.BoletaDTO;
import com.botica.botica.entity.Boleta;
import com.botica.botica.entity.Cliente;
import com.botica.botica.entity.Pedido;
import com.botica.botica.entity.Usuario;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.BoletaRepository;
import com.botica.botica.repository.PedidoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final PedidoRepository pedidoRepository;
    private final ObjectMapper objectMapper;

    public List<Boleta> findAll() {
        return boletaRepository.findAll();
    }

    public Page<Boleta> findAll(Pageable pageable) {
        Page<Integer> idPage = boletaRepository.findPageIds(pageable);
        if (idPage.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, idPage.getTotalElements());
        }

        List<Boleta> boletas = boletaRepository.findByIdBoletaIn(idPage.getContent());
        Map<Integer, Boleta> byId = boletas.stream()
                .collect(Collectors.toMap(Boleta::getIdBoleta, Function.identity()));

        List<Boleta> ordered = idPage.getContent().stream()
                .map(byId::get)
                .filter(java.util.Objects::nonNull)
                .toList();

        return new PageImpl<>(ordered, pageable, idPage.getTotalElements());
    }

    public Boleta findById(Integer id) {
        return boletaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta no encontrada con id: " + id));
    }

    public Boleta saveFromDto(BoletaDTO dto) {
        Boleta boleta = dto.getIdBoleta() != null
                ? findById(dto.getIdBoleta())
                : new Boleta();

        Pedido pedido = resolvePedido(dto.getIdPedido());

        boleta.setNumeroBoleta(dto.getNumeroBoleta());
        boleta.setPedido(pedido);
        if (boleta.getIdBoleta() == null) {
            boleta.setFechaEmision(LocalDateTime.now());
        }
        boleta.setTotal(pedido.getTotal() == null ? BigDecimal.ZERO : pedido.getTotal());
        boleta.setIgv(dto.getIgv() == null ? BigDecimal.ZERO : dto.getIgv());
        boleta.setDatosCliente(resolveDatosCliente(dto, pedido));
        boleta.setDatosEmpleado(resolveDatosEmpleado(dto, pedido));
        boleta.setImpresa(Boolean.TRUE.equals(dto.getImpresa()));

        Boleta saved = boletaRepository.save(boleta);
        return findById(saved.getIdBoleta());
    }

    private Pedido resolvePedido(Integer idPedido) {
        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + idPedido));
    }

    private String resolveDatosCliente(BoletaDTO dto, Pedido pedido) {
        if (dto.getDatosCliente() != null && !dto.getDatosCliente().isBlank()) {
            return dto.getDatosCliente();
        }

        Cliente cliente = pedido.getCliente();
        if (cliente == null) {
            return null;
        }

        Map<String, String> values = new LinkedHashMap<>();
        values.put("nombre", buildFullName(cliente.getNombre(), cliente.getApellido()));
        values.put("dni", cliente.getDni());
        return toJson(values);
    }

    private String resolveDatosEmpleado(BoletaDTO dto, Pedido pedido) {
        if (dto.getDatosEmpleado() != null && !dto.getDatosEmpleado().isBlank()) {
            return dto.getDatosEmpleado();
        }

        Usuario usuario = pedido.getUsuario();
        if (usuario == null) {
            return null;
        }

        Map<String, String> values = new LinkedHashMap<>();
        values.put("empleado", buildFullName(usuario.getNombre(), usuario.getApellido()));
        return toJson(values);
    }

    private String buildFullName(String nombre, String apellido) {
        String safeNombre = nombre == null ? "" : nombre.trim();
        String safeApellido = apellido == null ? "" : apellido.trim();
        return (safeNombre + " " + safeApellido).trim();
    }

    private String toJson(Map<String, String> values) {
        try {
            return objectMapper.writeValueAsString(values.entrySet().stream()
                    .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (left, right) -> right,
                            java.util.LinkedHashMap::new
                    )));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar los datos de la boleta", ex);
        }
    }

    public void deleteById(Integer id) {
        if (!boletaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Boleta no encontrada con id: " + id);
        }
        boletaRepository.deleteById(id);
    }
}
