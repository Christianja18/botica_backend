package com.botica.botica.service.importexport;

import com.botica.botica.dto.BoletaDTO;
import com.botica.botica.entity.Boleta;
import com.botica.botica.repository.BoletaRepository;
import com.botica.botica.service.BoletaService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BoletaImportExportHandler extends AbstractImportExportHandler {

    private final BoletaService boletaService;
    private final BoletaRepository boletaRepository;

    public BoletaImportExportHandler(Validator validator,
                                     BoletaService boletaService,
                                     BoletaRepository boletaRepository) {
        super(validator);
        this.boletaService = boletaService;
        this.boletaRepository = boletaRepository;
    }

    @Override
    public String resourceKey() {
        return "boletas";
    }

    @Override
    public List<String> headers() {
        return List.of(
                "id_boleta",
                "numero_boleta",
                "id_pedido",
                "fecha_emision",
                "total",
                "igv",
                "total_con_igv",
                "datos_cliente",
                "datos_empleado",
                "impresa"
        );
    }

    @Override
    public List<Map<String, String>> exportRows() {
        return boletaService.findAll().stream()
                .map(boleta -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("id_boleta", valueOf(boleta.getIdBoleta()));
                    row.put("numero_boleta", valueOf(boleta.getNumeroBoleta()));
                    row.put("id_pedido", valueOf(boleta.getPedido() != null ? boleta.getPedido().getIdPedido() : null));
                    row.put("fecha_emision", valueOf(boleta.getFechaEmision()));
                    row.put("total", valueOf(boleta.getTotal()));
                    row.put("igv", valueOf(boleta.getIgv()));
                    row.put("total_con_igv", valueOf(boleta.getTotalConIgv()));
                    row.put("datos_cliente", valueOf(boleta.getDatosCliente()));
                    row.put("datos_empleado", valueOf(boleta.getDatosEmpleado()));
                    row.put("impresa", valueOf(boleta.getImpresa()));
                    return row;
                })
                .toList();
    }

    @Override
    protected ImportAction importRow(Map<String, String> row) {
        Integer idBoleta = optionalInteger(row, "id_boleta");
        String numeroBoleta = requiredString(row, "numero_boleta");

        Boleta boleta = resolveBoleta(idBoleta, numeroBoleta);
        boolean updating = boleta != null;
        BigDecimal igv = optionalDecimal(row, "igv");
        Boolean impresa = optionalBoolean(row, "impresa");

        BoletaDTO dto = BoletaDTO.builder()
                .idBoleta(updating ? boleta.getIdBoleta() : null)
                .numeroBoleta(numeroBoleta)
                .idPedido(requiredInteger(row, "id_pedido"))
                .igv(igv != null ? igv : BigDecimal.ZERO)
                .datosCliente(optionalString(row, "datos_cliente"))
                .datosEmpleado(optionalString(row, "datos_empleado"))
                .impresa(impresa != null ? impresa : Boolean.FALSE)
                .build();

        validate(dto);
        boletaService.saveFromDto(dto);
        return updating ? ImportAction.UPDATED : ImportAction.INSERTED;
    }

    private Boleta resolveBoleta(Integer idBoleta, String numeroBoleta) {
        if (idBoleta != null) {
            return boletaService.findById(idBoleta);
        }
        return boletaRepository.findByNumeroBoleta(numeroBoleta).orElse(null);
    }
}
