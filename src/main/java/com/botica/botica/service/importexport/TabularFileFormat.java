package com.botica.botica.service.importexport;

import com.botica.botica.exception.BadRequestException;

public enum TabularFileFormat {
    CSV("csv", "text/csv"),
    EXCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final String extension;
    private final String contentType;

    TabularFileFormat(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String extension() {
        return extension;
    }

    public String contentType() {
        return contentType;
    }

    public String label() {
        return name().toLowerCase();
    }

    public static TabularFileFormat from(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("El formato es obligatorio. Use csv o excel");
        }

        String normalized = value.trim().toLowerCase();
        if ("csv".equals(normalized)) {
            return CSV;
        }
        if ("excel".equals(normalized) || "xlsx".equals(normalized)) {
            return EXCEL;
        }

        throw new BadRequestException("Formato no soportado: " + value + ". Use csv o excel");
    }
}
