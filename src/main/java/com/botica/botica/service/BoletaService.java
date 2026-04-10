package com.botica.botica.service;

import com.botica.botica.dto.BoletaDTO;
import com.botica.botica.entity.Boleta;
import com.botica.botica.entity.Cliente;
import com.botica.botica.entity.Pedido;
import com.botica.botica.entity.Usuario;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.BoletaRepository;
import com.botica.botica.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final PedidoRepository pedidoRepository;

    public List<Boleta> findAll() {
        return boletaRepository.findAll();
    }

    public Page<Boleta> findAll(Pageable pageable) {
        return boletaRepository.findAll(pageable);
    }

    public Boleta findById(Integer id) {
        return boletaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta no encontrada con id: " + id));
    }

    public Boleta saveFromDto(BoletaDTO dto) {
        Boleta boleta = dto.getIdBoleta() != null
                ? findById(dto.getIdBoleta())
                : new Boleta();

        Pedido pedido = pedidoRepository.findById(dto.getIdPedido())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + dto.getIdPedido()));

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

    private String resolveDatosCliente(BoletaDTO dto, Pedido pedido) {
        if (dto.getDatosCliente() != null && !dto.getDatosCliente().isBlank()) {
            return dto.getDatosCliente();
        }

        Cliente cliente = pedido.getCliente();
        if (cliente == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("{\"nombre\":\"")
                .append(cliente.getNombre())
                .append(' ')
                .append(cliente.getApellido())
                .append('"');
        if (cliente.getDni() != null && !cliente.getDni().isBlank()) {
            builder.append(",\"dni\":\"")
                    .append(cliente.getDni())
                    .append('"');
        }
        builder.append('}');
        return builder.toString();
    }

    private String resolveDatosEmpleado(BoletaDTO dto, Pedido pedido) {
        if (dto.getDatosEmpleado() != null && !dto.getDatosEmpleado().isBlank()) {
            return dto.getDatosEmpleado();
        }

        Usuario usuario = pedido.getUsuario();
        if (usuario == null) {
            return null;
        }

        return "{\"empleado\":\"" + usuario.getNombre() + ' ' + usuario.getApellido() + "\"}";
    }

    public void deleteById(Integer id) {
        if (!boletaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Boleta no encontrada con id: " + id);
        }
        boletaRepository.deleteById(id);
    }
}
