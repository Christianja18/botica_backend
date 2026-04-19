package com.botica.botica.dto;

import java.math.BigDecimal;

public record ResumenPeriodoDTO(
        Integer anio,
        Integer periodo,
        String etiqueta,
        BigDecimal valor
) {
}
