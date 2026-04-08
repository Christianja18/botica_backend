package com.botica.botica.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRol;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column
    private String descripcion;

    @NotNull(message = "El permiso de vender es obligatorio")
    @Column(nullable = false)
    private Boolean puedeVender = true;

    @NotNull(message = "El permiso de administrar inventario es obligatorio")
    @Column(nullable = false)
    private Boolean puedeAdministrarInventario = false;

    @NotNull(message = "El permiso de ver reportes es obligatorio")
    @Column(nullable = false)
    private Boolean puedeVerReportes = false;

    @NotNull(message = "El permiso de administrar usuarios es obligatorio")
    @Column(nullable = false)
    private Boolean puedeAdministrarUsuarios = false;

    @Column(nullable = false)
    private Boolean activo = true;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}