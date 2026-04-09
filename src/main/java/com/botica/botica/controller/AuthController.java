package com.botica.botica.controller;

import com.botica.botica.dto.AuthResponseDTO;
import com.botica.botica.dto.LoginRequestDTO;
import com.botica.botica.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login, sesión actual y cierre de sesión")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida email y contraseña contra el usuario registrado")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener sesión actual", description = "Retorna la sesión autenticada usando el token Bearer enviado")
    public ResponseEntity<AuthResponseDTO> me(@RequestHeader("Authorization") String authorizationHeader) {
        return ResponseEntity.ok(authService.getCurrentSession(authService.extractToken(authorizationHeader)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida el token Bearer actual")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        authService.logout(authService.extractToken(authorizationHeader));
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }
}
