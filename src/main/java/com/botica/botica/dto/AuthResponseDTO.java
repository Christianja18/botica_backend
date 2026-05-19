package com.botica.botica.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    @Schema(description = "JWT firmado. Enviar como Authorization: Bearer <token>", accessMode = Schema.AccessMode.READ_ONLY)
    private String token;
    @Schema(description = "Tipo de token", example = "Bearer", accessMode = Schema.AccessMode.READ_ONLY)
    private String tokenType;
    @Schema(description = "Fecha local de expiracion del token", example = "08/05/2026 18:30", accessMode = Schema.AccessMode.READ_ONLY)
    private String expiresAt;
    @Schema(description = "Usuario autenticado", accessMode = Schema.AccessMode.READ_ONLY)
    private AuthUserDTO usuario;
}
