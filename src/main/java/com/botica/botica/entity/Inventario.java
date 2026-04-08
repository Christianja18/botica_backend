package com.botica.botica.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventario")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idInventario;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false, unique = true)
    private Producto producto;

    @Column(nullable = false)
    private Integer stockActual = 0;

    @Column(nullable = false)
    private Integer stockMinimo = 10;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();
}
