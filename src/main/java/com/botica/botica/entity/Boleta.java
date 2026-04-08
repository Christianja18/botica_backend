package com.botica.botica.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "boletas")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idBoleta;

    @Column(nullable = false, unique = true, length = 20)
    private String numeroBoleta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    @Column(nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(precision = 10, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(name = "total_con_igv", precision = 10, scale = 2, insertable = false, updatable = false)
    private BigDecimal totalConIgv;

    @Column(columnDefinition = "TEXT")
    private String datosCliente;

    @Column(columnDefinition = "TEXT")
    private String datosEmpleado;

    @Column(nullable = false)
    private Boolean impresa = false;
}
