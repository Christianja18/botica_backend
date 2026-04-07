package com.botica.botica.service;

import com.botica.botica.entity.Usuario;
import com.botica.botica.exception.BadRequestException;
import com.botica.botica.exception.UsuarioNotFoundException;
import com.botica.botica.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con id: " + id));
    }

    public Usuario save(Usuario usuario) {
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

        if (usuario.getPasswordHash() != null && !isPasswordEncoded(usuario.getPasswordHash())) {
            usuario.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash()));
        }

        return usuarioRepository.save(usuario);
    }

    private boolean isPasswordEncoded(String password) {
        return password != null && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }

    public boolean matchesPassword(String rawPassword, Usuario usuario) {
        return passwordEncoder.matches(rawPassword, usuario.getPasswordHash());
    }

    public void deleteById(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNotFoundException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}