package com.botica.botica.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reportes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idReporte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoReporte tipoReporte;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @Column(nullable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    @JsonFormat(pattern = "dd/MM/yyyy")
    @Column
    private LocalDateTime fechaInicio;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @Column
    private LocalDateTime fechaFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generado_por", nullable = false)
    private Usuario generadoPor;

    @Column(columnDefinition = "JSON")
    private String datos;  // JSON string

    @Column(length = 500)
    private String archivoPath;

    public enum TipoReporte {
        ventas, inventario, pedidos, clientes
    }
}