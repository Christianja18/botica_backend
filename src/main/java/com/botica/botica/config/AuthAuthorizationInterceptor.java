package com.botica.botica.config;

import com.botica.botica.entity.Rol;
import com.botica.botica.entity.Usuario;
import com.botica.botica.exception.ForbiddenException;
import com.botica.botica.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthAuthorizationInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestPath = request.getRequestURI();
        if (isPublicPath(requestPath)) {
            return true;
        }

        String token = authService.extractToken(request.getHeader("Authorization"));
        Usuario usuario = authService.getAuthenticatedUser(token);
        validatePermission(requestPath, usuario.getRol());
        return true;
    }

    private boolean isPublicPath(String requestPath) {
        return "/api/auth/login".equals(requestPath)
                || requestPath.startsWith("/swagger-ui")
                || requestPath.startsWith("/v3/api-docs")
                || requestPath.startsWith("/api-docs");
    }

    private void validatePermission(String requestPath, Rol rol) {
        if (requestPath.startsWith("/api/auth/")) {
            return;
        }

        if (rol == null || !Boolean.TRUE.equals(rol.getActivo())) {
            throw new ForbiddenException("No tienes permisos para realizar esta accion");
        }

        if (requestPath.startsWith("/api/usuarios") || requestPath.startsWith("/api/roles")) {
            require(Boolean.TRUE.equals(rol.getPuedeAdministrarUsuarios()),
                    "No tienes permisos para administrar usuarios");
            return;
        }

        if (requestPath.startsWith("/api/backups")) {
            require(Boolean.TRUE.equals(rol.getPuedeAdministrarUsuarios()),
                    "No tienes permisos para generar respaldos de la base de datos");
            return;
        }

        if (requestPath.startsWith("/api/reportes")) {
            require(Boolean.TRUE.equals(rol.getPuedeVerReportes()),
                    "No tienes permisos para ver reportes");
            return;
        }

        if (requestPath.startsWith("/api/productos")
                || requestPath.startsWith("/api/inventario")
                || requestPath.startsWith("/api/categorias")
                || requestPath.startsWith("/api/proveedores")
                || requestPath.startsWith("/api/detalles-pedido")) {
            require(Boolean.TRUE.equals(rol.getPuedeAdministrarInventario()),
                    "No tienes permisos para administrar inventario");
            return;
        }

        if (requestPath.startsWith("/api/pedidos")
                || requestPath.startsWith("/api/boletas")
                || requestPath.startsWith("/api/clientes")) {
            require(Boolean.TRUE.equals(rol.getPuedeVender()),
                    "No tienes permisos para gestionar ventas");
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ForbiddenException(message);
        }
    }
}
