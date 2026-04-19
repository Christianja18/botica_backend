package com.botica.botica.service.importexport;

import com.botica.botica.dto.ClienteDTO;
import com.botica.botica.entity.Cliente;
import com.botica.botica.exception.BadRequestException;
import com.botica.botica.repository.ClienteRepository;
import com.botica.botica.service.ClienteService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClienteImportExportHandler extends AbstractImportExportHandler {

    private final ClienteService clienteService;
    private final ClienteRepository clienteRepository;

    public ClienteImportExportHandler(Validator validator,
                                      ClienteService clienteService,
                                      ClienteRepository clienteRepository) {
        super(validator);
        this.clienteService = clienteService;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public String resourceKey() {
        return "clientes";
    }

    @Override
    public List<String> headers() {
        return List.of("nombre", "apellido", "dni", "telefono", "email", "fecha_creacion");
    }

    @Override
    public List<Map<String, String>> exportRows() {
        return clienteService.findAll().stream()
                .map(cliente -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("nombre", valueOf(cliente.getNombre()));
                    row.put("apellido", valueOf(cliente.getApellido()));
                    row.put("dni", valueOf(cliente.getDni()));
                    row.put("telefono", valueOf(cliente.getTelefono()));
                    row.put("email", valueOf(cliente.getEmail()));
                    row.put("fecha_creacion", valueOf(cliente.getFechaCreacion()));
                    return row;
                })
                .toList();
    }

    @Override
    protected ImportAction importRow(Map<String, String> row) {
        Integer idCliente = optionalInteger(row, "id_cliente");
        String dni = optionalString(row, "dni");
        String email = optionalString(row, "email");

        Cliente cliente = resolveCliente(idCliente, dni, email);
        boolean updating = cliente != null;
        if (!updating) {
            cliente = new Cliente();
        }

        validateUniqueDni(dni, cliente.getIdCliente());

        ClienteDTO dto = new ClienteDTO(
                cliente.getIdCliente(),
                requiredString(row, "nombre"),
                requiredString(row, "apellido"),
                dni,
                optionalString(row, "telefono"),
                optionalString(row, "email"),
                null
        );
        validate(dto);

        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setDni(dto.getDni());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());

        clienteService.save(cliente);
        return updating ? ImportAction.UPDATED : ImportAction.INSERTED;
    }

    private Cliente resolveCliente(Integer idCliente, String dni, String email) {
        if (idCliente != null) {
            return clienteService.findById(idCliente);
        }
        if (dni != null) {
            return clienteRepository.findByDni(dni).orElse(null);
        }
        if (email != null) {
            return clienteRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    private void validateUniqueDni(String dni, Integer currentId) {
        if (dni == null) {
            return;
        }
        clienteRepository.findByDni(dni)
                .filter(existing -> currentId == null || !existing.getIdCliente().equals(currentId))
                .ifPresent(existing -> {
                    throw new BadRequestException("Ya existe un cliente con el DNI " + dni);
                });
    }
}
