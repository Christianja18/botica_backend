package com.botica.botica.service.importexport;

import com.botica.botica.dto.DetallePedidoDTO;
import com.botica.botica.entity.DetallePedido;
import com.botica.botica.entity.Producto;
import com.botica.botica.exception.BadRequestException;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.DetallePedidoRepository;
import com.botica.botica.repository.ProductoRepository;
import com.botica.botica.service.DetallePedidoService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DetallePedidoImportExportHandler extends AbstractImportExportHandler {

    private final DetallePedidoService detallePedidoService;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoRepository productoRepository;

    public DetallePedidoImportExportHandler(Validator validator,
                                            DetallePedidoService detallePedidoService,
                                            DetallePedidoRepository detallePedidoRepository,
                                            ProductoRepository productoRepository) {
        super(validator);
        this.detallePedidoService = detallePedidoService;
        this.detallePedidoRepository = detallePedidoRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public String resourceKey() {
        return "detalles-pedido";
    }

    @Override
    public List<String> headers() {
        return List.of("id_detalle", "id_pedido", "id_producto", "producto_codigo_barras", "cantidad", "precio_unitario", "subtotal");
    }

    @Override
    public List<Map<String, String>> exportRows() {
        return detallePedidoService.findAll().stream()
                .map(detalle -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("id_detalle", valueOf(detalle.getIdDetalle()));
                    row.put("id_pedido", valueOf(detalle.getPedido() != null ? detalle.getPedido().getIdPedido() : null));
                    row.put("id_producto", valueOf(detalle.getProducto() != null ? detalle.getProducto().getIdProducto() : null));
                    row.put("producto_codigo_barras", valueOf(detalle.getProducto() != null ? detalle.getProducto().getCodigoBarras() : null));
                    row.put("cantidad", valueOf(detalle.getCantidad()));
                    row.put("precio_unitario", valueOf(detalle.getPrecioUnitario()));
                    row.put("subtotal", valueOf(detalle.getSubtotal()));
                    return row;
                })
                .toList();
    }

    @Override
    protected ImportAction importRow(Map<String, String> row) {
        Integer idDetalle = optionalInteger(row, "id_detalle");
        Integer idPedido = requiredInteger(row, "id_pedido");
        Producto producto = resolveProducto(row);

        DetallePedido detalle = resolveDetalle(idDetalle, idPedido, producto.getIdProducto());
        boolean updating = detalle != null;

        DetallePedidoDTO dto = DetallePedidoDTO.builder()
                .idDetalle(updating ? detalle.getIdDetalle() : null)
                .idPedido(idPedido)
                .idProducto(producto.getIdProducto())
                .cantidad(optionalInteger(row, "cantidad"))
                .precioUnitario(optionalDecimal(row, "precio_unitario"))
                .build();

        validate(dto);
        detallePedidoService.saveFromDto(dto);
        return updating ? ImportAction.UPDATED : ImportAction.INSERTED;
    }

    private Producto resolveProducto(Map<String, String> row) {
        Integer idProducto = optionalInteger(row, "id_producto");
        if (idProducto != null) {
            return productoRepository.findById(idProducto)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + idProducto));
        }

        String codigoBarras = optionalString(row, "producto_codigo_barras");
        if (codigoBarras != null) {
            return productoRepository.findByCodigoBarras(codigoBarras)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con codigo de barras: " + codigoBarras));
        }

        throw new BadRequestException("Debe indicar id_producto o producto_codigo_barras");
    }

    private DetallePedido resolveDetalle(Integer idDetalle, Integer idPedido, Integer idProducto) {
        if (idDetalle != null) {
            return detallePedidoService.findById(idDetalle);
        }

        List<DetallePedido> matches = detallePedidoRepository.findByPedidoIdPedidoAndProductoIdProducto(idPedido, idProducto);
        if (matches.size() > 1) {
            throw new BadRequestException("Existe mas de un detalle para el pedido " + idPedido + " y el producto " + idProducto + ". Use id_detalle");
        }
        return matches.isEmpty() ? null : matches.get(0);
    }
}
