package com.botica.botica.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoletaDTO {

    private Integer idBoleta;

    @NotBlank(message = "El numero de boleta es obligatorio")
    @Size(max = 20, message = "El numero de boleta no puede exceder 20 caracteres")
    private String numeroBoleta;

    @NotNull(message = "El pedido es obligatorio")
    private Integer idPedido;

    private BoletaPedidoDTO pedido;

    private String fechaEmision;

    @DecimalMin(value = "0.00", message = "El total no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El total debe tener maximo 8 digitos enteros y 2 decimales")
    private BigDecimal total;

    @DecimalMin(value = "0.00", message = "El IGV no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El IGV debe tener maximo 8 digitos enteros y 2 decimales")
    @Builder.Default
    private BigDecimal igv = BigDecimal.ZERO;

    private BigDecimal totalConIgv;

    private String datosCliente;

    private String datosEmpleado;

    @Builder.Default
    private Boolean impresa = false;
}
