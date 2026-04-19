package com.botica.botica.service.importexport;

import com.botica.botica.dto.ProveedorDTO;
import com.botica.botica.entity.Proveedor;
import com.botica.botica.exception.BadRequestException;
import com.botica.botica.repository.ProveedorRepository;
import com.botica.botica.service.ProveedorService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProveedorImportExportHandler extends AbstractImportExportHandler {

    private final ProveedorService proveedorService;
    private final ProveedorRepository proveedorRepository;

    public ProveedorImportExportHandler(Validator validator,
                                        ProveedorService proveedorService,
                                        ProveedorRepository proveedorRepository) {
        super(validator);
        this.proveedorService = proveedorService;
        this.proveedorRepository = proveedorRepository;
    }

    @Override
    public String resourceKey() {
        return "proveedores";
    }

    @Override
    public List<String> headers() {
        return List.of("nombre", "ruc", "telefono", "email", "direccion", "fecha_creacion");
    }

    @Override
    public List<Map<String, String>> exportRows() {
        return proveedorService.findAll().stream()
                .map(proveedor -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("nombre", valueOf(proveedor.getNombre()));
                    row.put("ruc", valueOf(proveedor.getRuc()));
                    row.put("telefono", valueOf(proveedor.getTelefono()));
                    row.put("email", valueOf(proveedor.getEmail()));
                    row.put("direccion", valueOf(proveedor.getDireccion()));
                    row.put("fecha_creacion", valueOf(proveedor.getFechaCreacion()));
                    return row;
                })
                .toList();
    }

    @Override
    protected ImportAction importRow(Map<String, String> row) {
        Integer idProveedor = optionalInteger(row, "id_proveedor");
        String ruc = requiredString(row, "ruc");

        Proveedor proveedor = resolveProveedor(idProveedor, ruc);
        boolean updating = proveedor != null;
        if (!updating) {
            proveedor = new Proveedor();
        }

        validateUniqueRuc(ruc, proveedor.getIdProveedor());

        ProveedorDTO dto = new ProveedorDTO(
                proveedor.getIdProveedor(),
                requiredString(row, "nombre"),
                ruc,
                optionalString(row, "telefono"),
                optionalString(row, "email"),
                optionalString(row, "direccion"),
                null
        );
        validate(dto);

        proveedor.setNombre(dto.getNombre());
        proveedor.setRuc(dto.getRuc());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setEmail(dto.getEmail());
        proveedor.setDireccion(dto.getDireccion());

        proveedorService.save(proveedor);
        return updating ? ImportAction.UPDATED : ImportAction.INSERTED;
    }

    private Proveedor resolveProveedor(Integer idProveedor, String ruc) {
        if (idProveedor != null) {
            return proveedorService.findById(idProveedor);
        }
        return proveedorRepository.findByRuc(ruc).orElse(null);
    }

    private void validateUniqueRuc(String ruc, Integer currentId) {
        proveedorRepository.findByRuc(ruc)
                .filter(existing -> currentId == null || !existing.getIdProveedor().equals(currentId))
                .ifPresent(existing -> {
                    throw new BadRequestException("Ya existe un proveedor con el RUC " + ruc);
                });
    }
}
