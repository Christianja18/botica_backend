package com.botica.botica.service.importexport;

import com.botica.botica.dto.ProductoDTO;
import com.botica.botica.entity.Categoria;
import com.botica.botica.entity.Producto;
import com.botica.botica.entity.Proveedor;
import com.botica.botica.exception.BadRequestException;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.CategoriaRepository;
import com.botica.botica.repository.ProductoRepository;
import com.botica.botica.repository.ProveedorRepository;
import com.botica.botica.service.ProductoService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductoImportExportHandler extends AbstractImportExportHandler {

    private final ProductoService productoService;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;

    public ProductoImportExportHandler(Validator validator,
                                       ProductoService productoService,
                                       ProductoRepository productoRepository,
                                       CategoriaRepository categoriaRepository,
                                       ProveedorRepository proveedorRepository) {
        super(validator);
        this.productoService = productoService;
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.proveedorRepository = proveedorRepository;
    }

    @Override
    public String resourceKey() {
        return "productos";
    }

    @Override
    public List<String> headers() {
        return List.of(
                "id_producto",
                "nombre",
                "codigo_barras",
                "descripcion",
                "precio_venta",
                "precio_compra",
                "id_categoria",
                "categoria_nombre",
                "id_proveedor",
                "proveedor_ruc",
                "requiere_receta",
                "fecha_vencimiento",
                "fecha_creacion"
        );
    }

    @Override
    public List<Map<String, String>> exportRows() {
        return productoService.findAll().stream()
                .map(producto -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("id_producto", valueOf(producto.getIdProducto()));
                    row.put("nombre", valueOf(producto.getNombre()));
                    row.put("codigo_barras", valueOf(producto.getCodigoBarras()));
                    row.put("descripcion", valueOf(producto.getDescripcion()));
                    row.put("precio_venta", valueOf(producto.getPrecioVenta()));
                    row.put("precio_compra", valueOf(producto.getPrecioCompra()));
                    row.put("id_categoria", valueOf(producto.getCategoria() != null ? producto.getCategoria().getIdCategoria() : null));
                    row.put("categoria_nombre", valueOf(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null));
                    row.put("id_proveedor", valueOf(producto.getProveedor() != null ? producto.getProveedor().getIdProveedor() : null));
                    row.put("proveedor_ruc", valueOf(producto.getProveedor() != null ? producto.getProveedor().getRuc() : null));
                    row.put("requiere_receta", valueOf(producto.getRequiereReceta()));
                    row.put("fecha_vencimiento", valueOf(producto.getFechaVencimiento()));
                    row.put("fecha_creacion", valueOf(producto.getFechaCreacion()));
                    return row;
                })
                .toList();
    }

    @Override
    protected ImportAction importRow(Map<String, String> row) {
        Integer idProducto = optionalInteger(row, "id_producto");
        String codigoBarras = requiredString(row, "codigo_barras");

        Producto producto = resolveProducto(idProducto, codigoBarras);
        boolean updating = producto != null;

        Categoria categoria = resolveCategoria(row);
        Proveedor proveedor = resolveProveedor(row);
        Boolean requiereReceta = optionalBoolean(row, "requiere_receta");

        ProductoDTO dto = ProductoDTO.builder()
                .idProducto(updating ? producto.getIdProducto() : null)
                .nombre(requiredString(row, "nombre"))
                .codigoBarras(codigoBarras)
                .descripcion(optionalString(row, "descripcion"))
                .precioVenta(optionalDecimal(row, "precio_venta"))
                .precioCompra(optionalDecimal(row, "precio_compra"))
                .idCategoria(categoria.getIdCategoria())
                .idProveedor(proveedor.getIdProveedor())
                .requiereReceta(requiereReceta != null ? requiereReceta : Boolean.FALSE)
                .fechaVencimiento(optionalString(row, "fecha_vencimiento"))
                .build();

        validate(dto);
        productoService.saveFromDto(dto);
        return updating ? ImportAction.UPDATED : ImportAction.INSERTED;
    }

    private Producto resolveProducto(Integer idProducto, String codigoBarras) {
        if (idProducto != null) {
            return productoService.findById(idProducto);
        }
        return productoRepository.findByCodigoBarras(codigoBarras).orElse(null);
    }

    private Categoria resolveCategoria(Map<String, String> row) {
        Integer idCategoria = optionalInteger(row, "id_categoria");
        if (idCategoria != null) {
            return categoriaRepository.findById(idCategoria)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con id: " + idCategoria));
        }

        String nombre = optionalString(row, "categoria_nombre");
        if (nombre != null) {
            return categoriaRepository.findByNombreIgnoreCase(nombre)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con nombre: " + nombre));
        }

        throw new BadRequestException("Debe indicar id_categoria o categoria_nombre");
    }

    private Proveedor resolveProveedor(Map<String, String> row) {
        Integer idProveedor = optionalInteger(row, "id_proveedor");
        if (idProveedor != null) {
            return proveedorRepository.findById(idProveedor)
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con id: " + idProveedor));
        }

        String ruc = optionalString(row, "proveedor_ruc");
        if (ruc != null) {
            return proveedorRepository.findByRuc(ruc)
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con RUC: " + ruc));
        }

        throw new BadRequestException("Debe indicar id_proveedor o proveedor_ruc");
    }
}
