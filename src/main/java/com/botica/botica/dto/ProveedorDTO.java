package com.botica.botica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(min = 11, max = 11, message = "El RUC debe tener exactamente 11 digitos")
    @Pattern(regexp = "\\d{11}", message = "El RUC debe contener solo digitos")
    private String ruc;

    @Size(min = 9, max = 9, message = "El telefono debe tener exactamente 9 digitos")
    @Pattern(regexp = "\\d{9}", message = "El telefono debe contener solo 9 digitos")
    private String telefono;

    @Email(message = "El email debe tener un formato valido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;

    @Size(max = 500, message = "La direccion no puede exceder 500 caracteres")
    private String direccion;

    private String fechaCreacion;
}
