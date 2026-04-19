package com.botica.botica.service.importexport;

import com.botica.botica.dto.DetallePedidoDTO;
import com.botica.botica.entity.DetallePedido;
import com.botica.botica.entity.Producto;
import com.botica.botica.exception.BadRequestException;
import com.botica.botica.repository.DetallePedidoRepository;
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
    private final ImportExportLookupService lookupService;

    public DetallePedidoImportExportHandler(Validator validator,
                                            DetallePedidoService detallePedidoService,
                                            DetallePedidoRepository detallePedidoRepository,
                                            ImportExportLookupService lookupService) {
        super(validator);
        this.detallePedidoService = detallePedidoService;
        this.detallePedidoRepository = detallePedidoRepository;
        this.lookupService = lookupService;
    }

    @Override
    public String resourceKey() {
        return "detalles-pedido";
    }

    @Override
    public List<String> headers() {
        return List.of(
                "pedido_fecha",
                "pedido_cliente",
                "pedido_cliente_dni",
                "pedido_usuario",
                "pedido_usuario_email",
                "producto_nombre",
                "producto_codigo_barras",
                "cantidad",
                "precio_unitario",
                "subtotal"
        );
    }

    @Override
    public List<Map<String, String>> exportRows() {
        return detallePedidoService.findAll().stream()
                .map(detalle -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("pedido_fecha", valueOf(detalle.getPedido() != null ? detalle.getPedido().getFechaPedido() : null));
                    row.put("pedido_cliente", valueOf(detalle.getPedido() != null && detalle.getPedido().getCliente() != null
                            ? lookupService.buildFullName(detalle.getPedido().getCliente().getNombre(), detalle.getPedido().getCliente().getApellido())
                            : null));
                    row.put("pedido_cliente_dni", valueOf(detalle.getPedido() != null && detalle.getPedido().getCliente() != null
                            ? detalle.getPedido().getCliente().getDni()
                            : null));
                    row.put("pedido_usuario", valueOf(detalle.getPedido() != null && detalle.getPedido().getUsuario() != null
                            ? lookupService.buildFullName(detalle.getPedido().getUsuario().getNombre(), detalle.getPedido().getUsuario().getApellido())
                            : null));
                    row.put("pedido_usuario_email", valueOf(detalle.getPedido() != null && detalle.getPedido().getUsuario() != null
                            ? detalle.getPedido().getUsuario().getEmail()
                            : null));
                    row.put("producto_nombre", valueOf(detalle.getProducto() != null ? detalle.getProducto().getNombre() : null));
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
        Integer idPedido = resolvePedidoId(row);
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
        return lookupService.resolveProducto(
                optionalInteger(row, "id_producto"),
                optionalString(row, "producto_codigo_barras"),
                optionalString(row, "producto_nombre")
        );
    }

    private Integer resolvePedidoId(Map<String, String> row) {
        return lookupService.resolvePedidoId(
                optionalInteger(row, "id_pedido"),
                requiredDateTime(row, "pedido_fecha"),
                optionalString(row, "pedido_usuario_email"),
                optionalString(row, "pedido_cliente_dni")
        );
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
