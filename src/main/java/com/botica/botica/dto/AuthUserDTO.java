package com.botica.botica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUserDTO {

    private Integer idUsuario;
    private String nombre;
    private String apellido;
    private String nombreCompleto;
    private String email;
    private Boolean activo;
    private Integer idRol;
    private String rolNombre;
    private Boolean puedeVender;
    private Boolean puedeAdministrarInventario;
    private Boolean puedeVerReportes;
    private Boolean puedeAdministrarUsuarios;
}
