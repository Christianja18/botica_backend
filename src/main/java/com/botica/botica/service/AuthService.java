package com.botica.botica.service;

import com.botica.botica.dto.AuthResponseDTO;
import com.botica.botica.dto.AuthUserDTO;
import com.botica.botica.dto.LoginRequestDTO;
import com.botica.botica.entity.Rol;
import com.botica.botica.entity.Usuario;
import com.botica.botica.exception.UnauthorizedException;
import com.botica.botica.repository.UsuarioRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final long SESSION_HOURS = 8L;

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final ConcurrentHashMap<String, AuthSession> sessions = new ConcurrentHashMap<>();

    public AuthResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UnauthorizedException("El usuario está inactivo");
        }

        Rol rol = usuario.getRol();
        if (rol == null || !Boolean.TRUE.equals(rol.getActivo())) {
            throw new UnauthorizedException("El rol del usuario está inactivo");
        }

        if (!usuarioService.matchesPassword(request.getPassword(), usuario)) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(SESSION_HOURS);
        sessions.put(token, AuthSession.builder()
                .token(token)
                .userId(usuario.getIdUsuario())
                .expiresAt(expiresAt)
                .build());

        return buildResponse(token, expiresAt, usuario);
    }

    public AuthResponseDTO getCurrentSession(String token) {
        AuthSession session = getValidSession(token);
        Usuario usuario = usuarioRepository.findById(session.getUserId())
                .orElseThrow(() -> new UnauthorizedException("La sesión ya no es válida"));
        return buildResponse(session.getToken(), session.getExpiresAt(), usuario);
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Token de sesión no proporcionado");
        }
        sessions.remove(token);
    }

    public String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException("No se recibió el encabezado Authorization");
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("El formato del token es inválido");
        }
        return authorizationHeader.substring(7).trim();
    }

    private AuthSession getValidSession(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Token de sesión no proporcionado");
        }

        AuthSession session = sessions.get(token);
        if (session == null) {
            throw new UnauthorizedException("La sesión no existe o ya expiró");
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            sessions.remove(token);
            throw new UnauthorizedException("La sesión expiró");
        }

        return session;
    }

    private AuthResponseDTO buildResponse(String token, LocalDateTime expiresAt, Usuario usuario) {
        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresAt(expiresAt.format(DATE_TIME_FORMATTER))
                .usuario(buildUser(usuario))
                .build();
    }

    private AuthUserDTO buildUser(Usuario usuario) {
        Rol rol = usuario.getRol();
        return AuthUserDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .nombreCompleto(usuario.getNombre() + " " + usuario.getApellido())
                .email(usuario.getEmail())
                .activo(usuario.getActivo())
                .idRol(rol != null ? rol.getIdRol() : null)
                .rolNombre(rol != null ? rol.getNombre() : null)
                .puedeVender(rol != null ? rol.getPuedeVender() : false)
                .puedeAdministrarInventario(rol != null ? rol.getPuedeAdministrarInventario() : false)
                .puedeVerReportes(rol != null ? rol.getPuedeVerReportes() : false)
                .puedeAdministrarUsuarios(rol != null ? rol.getPuedeAdministrarUsuarios() : false)
                .build();
    }

    @Builder
    private static class AuthSession {
        private String token;
        private Integer userId;
        private LocalDateTime expiresAt;

        public String getToken() {
            return token;
        }

        public Integer getUserId() {
            return userId;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }
    }
}
