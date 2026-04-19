package com.botica.botica.service.importexport;

import com.botica.botica.dto.UsuarioDTO;
import com.botica.botica.entity.Rol;
import com.botica.botica.entity.Usuario;
import com.botica.botica.exception.BadRequestException;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.RolRepository;
import com.botica.botica.repository.UsuarioRepository;
import com.botica.botica.service.UsuarioService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UsuarioImportExportHandler extends AbstractImportExportHandler {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioImportExportHandler(Validator validator,
                                      UsuarioService usuarioService,
                                      UsuarioRepository usuarioRepository,
                                      RolRepository rolRepository) {
        super(validator);
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    public String resourceKey() {
        return "usuarios";
    }

    @Override
    public List<String> headers() {
        return List.of("nombre", "apellido", "email", "password", "activo", "rol_nombre", "fecha_creacion");
    }

    @Override
    public List<Map<String, String>> exportRows() {
        return usuarioService.findAll().stream()
                .map(usuario -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("nombre", valueOf(usuario.getNombre()));
                    row.put("apellido", valueOf(usuario.getApellido()));
                    row.put("email", valueOf(usuario.getEmail()));
                    row.put("password", "");
                    row.put("activo", valueOf(usuario.getActivo()));
                    row.put("rol_nombre", valueOf(usuario.getRol() != null ? usuario.getRol().getNombre() : null));
                    row.put("fecha_creacion", valueOf(usuario.getFechaCreacion()));
                    return row;
                })
                .toList();
    }

    @Override
    protected ImportAction importRow(Map<String, String> row) {
        Integer idUsuario = optionalInteger(row, "id_usuario");
        String email = requiredString(row, "email");

        Usuario usuario = resolveUsuario(idUsuario, email);
        boolean updating = usuario != null;
        Rol rol = resolveRol(row);
        Boolean activo = optionalBoolean(row, "activo");

        UsuarioDTO dto = UsuarioDTO.builder()
                .idUsuario(updating ? usuario.getIdUsuario() : null)
                .nombre(requiredString(row, "nombre"))
                .apellido(requiredString(row, "apellido"))
                .email(email)
                .passwordHash(optionalString(row, "password"))
                .activo(activo != null ? activo : Boolean.TRUE)
                .idRol(rol.getIdRol())
                .build();

        validate(dto);
        usuarioService.saveFromDto(dto);
        return updating ? ImportAction.UPDATED : ImportAction.INSERTED;
    }

    private Usuario resolveUsuario(Integer idUsuario, String email) {
        if (idUsuario != null) {
            return usuarioService.findById(idUsuario);
        }
        return usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
    }

    private Rol resolveRol(Map<String, String> row) {
        Integer idRol = optionalInteger(row, "id_rol");
        if (idRol != null) {
            return rolRepository.findById(idRol)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + idRol));
        }

        String nombre = optionalString(row, "rol_nombre");
        if (nombre != null) {
            return rolRepository.findByNombreIgnoreCase(nombre)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con nombre: " + nombre));
        }

        throw new BadRequestException("Debe indicar id_rol o rol_nombre");
    }
}
