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
public class ClienteDTO {

    private Integer idCliente;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String apellido;

    @Size(min = 8, max = 8, message = "El DNI debe tener exactamente 8 digitos")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe contener solo digitos")
    private String dni;

    @Size(min = 9, max = 9, message = "El telefono debe tener exactamente 9 digitos")
    @Pattern(regexp = "\\d{9}", message = "El telefono debe contener solo 9 digitos")
    private String telefono;

    @Email(message = "El email debe tener un formato valido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;

    private String fechaCreacion;
}
