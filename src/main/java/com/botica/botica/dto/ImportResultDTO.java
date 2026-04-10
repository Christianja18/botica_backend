package com.botica.botica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportResultDTO {

    private String recurso;

    private String formato;

    private int totalFilas;

    private int insertados;

    private int actualizados;

    private int fallidos;

    @Builder.Default
    private List<ImportErrorDTO> errores = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImportErrorDTO {
        private int fila;
        private String mensaje;
    }
}
