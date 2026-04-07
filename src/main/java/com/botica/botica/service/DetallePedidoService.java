package com.botica.botica.service;

import com.botica.botica.entity.DetallePedido;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.DetallePedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;

    public List<DetallePedido> findAll() {
        return detallePedidoRepository.findAll();
    }

    public DetallePedido findById(Integer id) {
        return detallePedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle de pedido no encontrado con id: " + id));
    }

    public DetallePedido save(DetallePedido detallePedido) {
        return detallePedidoRepository.save(detallePedido);
    }

    public void deleteById(Integer id) {
        if (!detallePedidoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Detalle de pedido no encontrado con id: " + id);
        }
        detallePedidoRepository.deleteById(id);
    }
}
