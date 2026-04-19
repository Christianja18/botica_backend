package com.botica.botica.service.importexport;

import com.botica.botica.entity.Pedido;
import com.botica.botica.entity.Producto;
import com.botica.botica.entity.Proveedor;
import com.botica.botica.exception.BadRequestException;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.PedidoRepository;
import com.botica.botica.repository.ProductoRepository;
import com.botica.botica.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportExportLookupService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;

    public Integer resolvePedidoId(Integer idPedido,
                                   LocalDateTime fechaPedido,
                                   String usuarioEmail,
                                   String clienteDni) {
        if (idPedido != null) {
            return idPedido;
        }

        if (fechaPedido == null) {
            throw new BadRequestException("Debe indicar id_pedido o pedido_fecha");
        }

        List<Pedido> matches = usuarioEmail != null
                ? pedidoRepository.findByFechaPedidoAndUsuarioEmailIgnoreCase(fechaPedido, usuarioEmail)
                : pedidoRepository.findByFechaPedido(fechaPedido);

        if (clienteDni != null) {
            matches = matches.stream()
                    .filter(pedido -> pedido.getCliente() != null && clienteDni.equals(pedido.getCliente().getDni()))
                    .toList();
        }

        if (matches.isEmpty()) {
            throw new ResourceNotFoundException("Pedido no encontrado con los datos proporcionados");
        }
        if (matches.size() > 1) {
            throw new BadRequestException("Existe mas de un pedido con los datos proporcionados. Use id_pedido o agregue pedido_cliente_dni/pedido_usuario_email");
        }
        return matches.get(0).getIdPedido();
    }

    public Producto resolveProducto(Integer idProducto, String codigoBarras, String nombre) {
        if (idProducto != null) {
            return productoRepository.findById(idProducto)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + idProducto));
        }

        if (codigoBarras != null) {
            return productoRepository.findByCodigoBarras(codigoBarras)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con codigo de barras: " + codigoBarras));
        }

        if (nombre != null) {
            return productoRepository.findByNombreIgnoreCase(nombre)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con nombre: " + nombre));
        }

        throw new BadRequestException("Debe indicar id_producto, producto_codigo_barras o producto_nombre");
    }

    public Proveedor resolveProveedor(Integer idProveedor, String ruc, String nombre) {
        if (idProveedor != null) {
            return proveedorRepository.findById(idProveedor)
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con id: " + idProveedor));
        }

        if (ruc != null) {
            return proveedorRepository.findByRuc(ruc)
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con RUC: " + ruc));
        }

        if (nombre != null) {
            return proveedorRepository.findByNombreIgnoreCase(nombre)
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con nombre: " + nombre));
        }

        throw new BadRequestException("Debe indicar id_proveedor, proveedor_ruc o proveedor_nombre");
    }

    public String buildFullName(String nombre, String apellido) {
        String safeNombre = nombre == null ? "" : nombre.trim();
        String safeApellido = apellido == null ? "" : apellido.trim();
        return (safeNombre + " " + safeApellido).trim();
    }
}
