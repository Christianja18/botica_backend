package com.botica.botica.mapper;

import com.botica.botica.dto.BoletaDTO;
import com.botica.botica.entity.Boleta;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class BoletaMapper {

    private final ModelMapper modelMapper;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public BoletaDTO toDTO(Boleta boleta) {
        if (boleta == null) {
            return null;
        }

        BoletaDTO dto = BoletaDTO.builder()
                .idBoleta(boleta.getIdBoleta())
                .numeroBoleta(boleta.getNumeroBoleta())
                .total(boleta.getTotal())
                .igv(boleta.getIgv())
                .totalConIgv(resolveTotalConIgv(boleta))
                .datosCliente(boleta.getDatosCliente())
                .datosEmpleado(boleta.getDatosEmpleado())
                .impresa(boleta.getImpresa())
                .build();

        if (boleta.getPedido() != null) {
            dto.setIdPedido(boleta.getPedido().getIdPedido());
        }

        if (boleta.getFechaEmision() != null) {
            dto.setFechaEmision(boleta.getFechaEmision().format(DATE_TIME_FORMATTER));
        }

        return dto;
    }

    private BigDecimal resolveTotalConIgv(Boleta boleta) {
        if (boleta.getTotalConIgv() != null) {
            return boleta.getTotalConIgv();
        }
        if (boleta.getTotal() == null) {
            return null;
        }
        return boleta.getTotal().add(boleta.getIgv() == null ? BigDecimal.ZERO : boleta.getIgv());
    }
}
