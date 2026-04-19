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
    private final ImportExportLookupService lookupService;

    public BoletaImportExportHandler(Validator validator,
                                     BoletaService boletaService,
                                     BoletaRepository boletaRepository,
                                     ImportExportLookupService lookupService) {
        super(validator);
        this.boletaService = boletaService;
        this.boletaRepository = boletaRepository;
        this.lookupService = lookupService;
    }

    @Override
    public String resourceKey() {
        return "boletas";
    }

    @Override
    public List<String> headers() {
        return List.of(
                "numero_boleta",
                "pedido_fecha",
                "pedido_cliente",
                "pedido_cliente_dni",
                "pedido_usuario",
                "pedido_usuario_email",
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
                    row.put("numero_boleta", valueOf(boleta.getNumeroBoleta()));
                    row.put("pedido_fecha", valueOf(boleta.getPedido() != null ? boleta.getPedido().getFechaPedido() : null));
                    row.put("pedido_cliente", valueOf(boleta.getPedido() != null && boleta.getPedido().getCliente() != null
                            ? lookupService.buildFullName(boleta.getPedido().getCliente().getNombre(), boleta.getPedido().getCliente().getApellido())
                            : null));
                    row.put("pedido_cliente_dni", valueOf(boleta.getPedido() != null && boleta.getPedido().getCliente() != null
                            ? boleta.getPedido().getCliente().getDni()
                            : null));
                    row.put("pedido_usuario", valueOf(boleta.getPedido() != null && boleta.getPedido().getUsuario() != null
                            ? lookupService.buildFullName(boleta.getPedido().getUsuario().getNombre(), boleta.getPedido().getUsuario().getApellido())
                            : null));
                    row.put("pedido_usuario_email", valueOf(boleta.getPedido() != null && boleta.getPedido().getUsuario() != null
                            ? boleta.getPedido().getUsuario().getEmail()
                            : null));
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
                .idPedido(resolvePedidoId(row))
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

    private Integer resolvePedidoId(Map<String, String> row) {
        return lookupService.resolvePedidoId(
                optionalInteger(row, "id_pedido"),
                requiredDateTime(row, "pedido_fecha"),
                optionalString(row, "pedido_usuario_email"),
                optionalString(row, "pedido_cliente_dni")
        );
    }
}
