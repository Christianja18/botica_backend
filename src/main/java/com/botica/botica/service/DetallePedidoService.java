package com.botica.botica.service;

import com.botica.botica.dto.DetallePedidoDTO;
import com.botica.botica.entity.DetallePedido;
import com.botica.botica.entity.Pedido;
import com.botica.botica.entity.Producto;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.DetallePedidoRepository;
import com.botica.botica.repository.PedidoRepository;
import com.botica.botica.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public List<DetallePedido> findAll() {
        return detallePedidoRepository.findAll();
    }

    public DetallePedido findById(Integer id) {
        return detallePedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle de pedido no encontrado con id: " + id));
    }

    public DetallePedido save(DetallePedido detallePedido) {
        DetallePedido saved = detallePedidoRepository.save(detallePedido);
        updatePedidoTotal(saved.getPedido().getIdPedido());
        return findById(saved.getIdDetalle());
    }

    public DetallePedido saveFromDto(DetallePedidoDTO dto) {
        Pedido pedido = pedidoRepository.findById(dto.getIdPedido())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + dto.getIdPedido()));
        Producto producto = productoRepository.findById(dto.getIdProducto())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + dto.getIdProducto()));

        DetallePedido detalle = dto.getIdDetalle() != null
                ? findById(dto.getIdDetalle())
                : new DetallePedido();

        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(dto.getCantidad());
        detalle.setPrecioUnitario(dto.getPrecioUnitario());

        return save(detalle);
    }

    public void deleteById(Integer id) {
        DetallePedido detalle = findById(id);
        Integer pedidoId = detalle.getPedido().getIdPedido();
        detallePedidoRepository.delete(detalle);
        updatePedidoTotal(pedidoId);
    }

    private void updatePedidoTotal(Integer pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId));

        BigDecimal total = detallePedidoRepository.findByPedidoIdPedido(pedidoId).stream()
                .map(detalle -> detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        pedido.setTotal(total);
        pedidoRepository.save(pedido);
    }
}
