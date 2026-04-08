package com.botica.botica.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorDTO {

    private Integer idProveedor;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombre;

    @NotBlank(message = "El RUC es obligatorio")
    @Size(min = 11, max = 11, message = "El RUC debe tener exactamente 11 dígitos")
    @Pattern(regexp = "\\d{11}", message = "El RUC debe contener solo dígitos")
    private String ruc;

    @Size(max = 15, message = "El teléfono no puede exceder 15 caracteres")
    private String telefono;

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;

    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String direccion;

    private String fechaCreacion;
}
