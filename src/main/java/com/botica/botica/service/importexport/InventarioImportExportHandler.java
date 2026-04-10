package com.botica.botica.service.importexport;

import com.botica.botica.dto.InventarioDTO;
import com.botica.botica.entity.Inventario;
import com.botica.botica.entity.Producto;
import com.botica.botica.exception.BadRequestException;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.InventarioRepository;
import com.botica.botica.repository.ProductoRepository;
import com.botica.botica.service.InventarioService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventarioImportExportHandler extends AbstractImportExportHandler {

    private final InventarioService inventarioService;
    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;

    public InventarioImportExportHandler(Validator validator,
                                         InventarioService inventarioService,
                                         InventarioRepository inventarioRepository,
                                         ProductoRepository productoRepository) {
        super(validator);
        this.inventarioService = inventarioService;
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public String resourceKey() {
        return "inventario";
    }

    @Override
    public List<String> headers() {
        return List.of("id_inventario", "id_producto", "producto_codigo_barras", "stock_actual", "stock_minimo", "fecha_actualizacion");
    }

    @Override
    public List<Map<String, String>> exportRows() {
        return inventarioService.findAll().stream()
                .map(inventario -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("id_inventario", valueOf(inventario.getIdInventario()));
                    row.put("id_producto", valueOf(inventario.getProducto() != null ? inventario.getProducto().getIdProducto() : null));
                    row.put("producto_codigo_barras", valueOf(inventario.getProducto() != null ? inventario.getProducto().getCodigoBarras() : null));
                    row.put("stock_actual", valueOf(inventario.getStockActual()));
                    row.put("stock_minimo", valueOf(inventario.getStockMinimo()));
                    row.put("fecha_actualizacion", valueOf(inventario.getFechaActualizacion()));
                    return row;
                })
                .toList();
    }

    @Override
    protected ImportAction importRow(Map<String, String> row) {
        Integer idInventario = optionalInteger(row, "id_inventario");
        Producto producto = resolveProducto(row);

        Inventario inventario = resolveInventario(idInventario, producto.getIdProducto());
        boolean updating = inventario != null;

        InventarioDTO dto = InventarioDTO.builder()
                .idInventario(updating ? inventario.getIdInventario() : null)
                .idProducto(producto.getIdProducto())
                .stockActual(optionalInteger(row, "stock_actual"))
                .stockMinimo(optionalInteger(row, "stock_minimo"))
                .build();

        validate(dto);
        inventarioService.saveFromDto(dto);
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

    private Inventario resolveInventario(Integer idInventario, Integer productoId) {
        if (idInventario != null) {
            return inventarioService.findById(idInventario);
        }
        return inventarioRepository.findByProductoIdProducto(productoId).orElse(null);
    }
}
