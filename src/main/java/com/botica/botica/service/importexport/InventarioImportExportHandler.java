package com.botica.botica.service.importexport;

import com.botica.botica.dto.InventarioDTO;
import com.botica.botica.entity.Inventario;
import com.botica.botica.entity.Producto;
import com.botica.botica.repository.InventarioRepository;
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
    private final ImportExportLookupService lookupService;

    public InventarioImportExportHandler(Validator validator,
                                         InventarioService inventarioService,
                                         InventarioRepository inventarioRepository,
                                         ImportExportLookupService lookupService) {
        super(validator);
        this.inventarioService = inventarioService;
        this.inventarioRepository = inventarioRepository;
        this.lookupService = lookupService;
    }

    @Override
    public String resourceKey() {
        return "inventario";
    }

    @Override
    public List<String> headers() {
        return List.of("producto_nombre", "producto_codigo_barras", "stock_actual", "stock_minimo", "fecha_actualizacion");
    }

    @Override
    public List<Map<String, String>> exportRows() {
        return inventarioService.findAll().stream()
                .map(inventario -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("producto_nombre", valueOf(inventario.getProducto() != null ? inventario.getProducto().getNombre() : null));
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
        return lookupService.resolveProducto(
                optionalInteger(row, "id_producto"),
                optionalString(row, "producto_codigo_barras"),
                optionalString(row, "producto_nombre")
        );
    }

    private Inventario resolveInventario(Integer idInventario, Integer productoId) {
        if (idInventario != null) {
            return inventarioService.findById(idInventario);
        }
        return inventarioRepository.findByProductoIdProducto(productoId).orElse(null);
    }
}
