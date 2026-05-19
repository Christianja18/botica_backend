package com.botica.botica.service;

import com.botica.botica.dto.UsuarioDTO;
import com.botica.botica.entity.Rol;
import com.botica.botica.entity.Usuario;
import com.botica.botica.exception.BadRequestException;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.exception.UsuarioNotFoundException;
import com.botica.botica.repository.RolRepository;
import com.botica.botica.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Page<Usuario> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    public Usuario findById(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con id: " + id));
    }

    public Usuario saveFromDto(UsuarioDTO dto) {
        Usuario usuario = dto.getIdUsuario() != null
                ? findById(dto.getIdUsuario())
                : new Usuario();

        Rol rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + dto.getIdRol()));

        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setEmail(dto.getEmail());
        usuario.setActivo(dto.getActivo());
        usuario.setRol(rol);

        if (dto.getPasswordHash() != null && !dto.getPasswordHash().isBlank()) {
            usuario.setPasswordHash(dto.getPasswordHash());
        } else if (usuario.getIdUsuario() == null) {
            throw new BadRequestException("La contraseña es obligatoria para crear un usuario");
        }

        if (usuario.getFechaCreacion() == null) {
            usuario.setFechaCreacion(LocalDateTime.now());
        }

        return save(usuario);
    }

    public Usuario save(Usuario usuario) {
        logger.info("Guardando usuario id={}", usuario.getIdUsuario());

        boolean emailTaken = usuarioRepository.existsByEmail(usuario.getEmail());
        if (usuario.getIdUsuario() == null) {
            if (emailTaken) {
                throw new BadRequestException("Email ya existe: " + usuario.getEmail());
            }
        } else {
            Optional<Usuario> existing = usuarioRepository.findById(usuario.getIdUsuario());
            if (existing.isPresent() && !existing.get().getEmail().equals(usuario.getEmail()) && emailTaken) {
                throw new BadRequestException("Email ya existe: " + usuario.getEmail());
            }
        }

        if (usuario.getRol() == null || usuario.getRol().getIdRol() == null) {
            throw new BadRequestException("El rol es obligatorio");
        }

        if (usuario.getPasswordHash() != null && !isPasswordEncoded(usuario.getPasswordHash())) {
            usuario.setPasswordHash(PASSWORD_ENCODER.encode(usuario.getPasswordHash()));
        }

        Usuario saved = usuarioRepository.save(usuario);
        logger.info("Usuario guardado exitosamente con id: {}", saved.getIdUsuario());
        return findById(saved.getIdUsuario());
    }

    public boolean matchesPassword(String rawPassword, Usuario usuario) {
        return usuario.getPasswordHash() != null && PASSWORD_ENCODER.matches(rawPassword, usuario.getPasswordHash());
    }

    public void deleteById(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNotFoundException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private boolean isPasswordEncoded(String passwordHash) {
        return passwordHash.startsWith("$2a$")
                || passwordHash.startsWith("$2b$")
                || passwordHash.startsWith("$2y$");
    }
}
