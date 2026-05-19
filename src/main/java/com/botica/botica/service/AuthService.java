package com.botica.botica.service;

import com.botica.botica.dto.AuthResponseDTO;
import com.botica.botica.dto.AuthUserDTO;
import com.botica.botica.dto.LoginRequestDTO;
import com.botica.botica.entity.Rol;
import com.botica.botica.entity.Usuario;
import com.botica.botica.exception.UnauthorizedException;
import com.botica.botica.repository.UsuarioRepository;
import com.botica.botica.service.support.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final JwtTokenService jwtTokenService;
    private final ConcurrentHashMap<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    @Value("${botica.auth.session-hours:8}")
    private long sessionHours;

    public AuthResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        validateAccess(usuario, request.getPassword());

        Instant expiresAt = Instant.now().plusSeconds(sessionHours * 3600);
        String token = jwtTokenService.generateToken(usuario.getIdUsuario(), expiresAt);

        return buildResponse(token, expiresAt, usuario);
    }

    public AuthResponseDTO getCurrentSession(String token) {
        JwtTokenService.JwtClaims claims = getValidClaims(token);
        Usuario usuario = loadActiveUser(claims.userId());
        return buildResponse(token, claims.expiresAt(), usuario);
    }

    public Usuario getAuthenticatedUser(String token) {
        JwtTokenService.JwtClaims claims = getValidClaims(token);
        return loadActiveUser(claims.userId());
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Token de sesion no proporcionado");
        }
        JwtTokenService.JwtClaims claims = getValidClaims(token);
        revokedTokens.put(token, claims.expiresAt());
        removeExpiredRevocations();
    }

    public String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException("No se recibio el encabezado Authorization");
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("El formato del token es invalido");
        }
        return authorizationHeader.substring(7).trim();
    }

    private void validateAccess(Usuario usuario, String rawPassword) {
        validateSessionUser(usuario);

        if (!usuarioService.matchesPassword(rawPassword, usuario)) {
            throw new UnauthorizedException("Credenciales inválidas");
        }
    }

    private JwtTokenService.JwtClaims getValidClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Token de sesion no proporcionado");
        }

        removeExpiredRevocations();
        if (revokedTokens.containsKey(token)) {
            throw new UnauthorizedException("La sesion ya fue cerrada");
        }

        return jwtTokenService.validateToken(token);
    }

    private Usuario loadActiveUser(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("La sesion ya no es valida"));
        validateSessionUser(usuario);
        return usuario;
    }

    private void validateSessionUser(Usuario usuario) {
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UnauthorizedException("El usuario esta inactivo");
        }

        Rol rol = usuario.getRol();
        if (rol == null || !Boolean.TRUE.equals(rol.getActivo())) {
            throw new UnauthorizedException("El rol del usuario esta inactivo");
        }
    }

    private AuthResponseDTO buildResponse(String token, Instant expiresAt, Usuario usuario) {
        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresAt(LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault()).format(DATE_TIME_FORMATTER))
                .usuario(buildUser(usuario))
                .build();
    }

    private AuthUserDTO buildUser(Usuario usuario) {
        Rol rol = usuario.getRol();
        return AuthUserDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .nombreCompleto(buildFullName(usuario))
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

    private String buildFullName(Usuario usuario) {
        String nombre = usuario.getNombre() == null ? "" : usuario.getNombre().trim();
        String apellido = usuario.getApellido() == null ? "" : usuario.getApellido().trim();
        return (nombre + " " + apellido).trim();
    }

    private void removeExpiredRevocations() {
        Instant now = Instant.now();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
