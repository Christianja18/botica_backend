package com.botica.botica.service.importexport;

public record ExportFileResult(
        String fileName,
        String contentType,
        byte[] content
) {
}
