package com.botica.botica.dto;

import java.math.BigDecimal;

public record ProductoMasVendidoDTO(
        Integer idProducto,
        String codigoBarras,
        String nombre,
        Integer idCategoria,
        String categoria,
        Integer cantidadVendida,
        BigDecimal totalVendido
) {
}
