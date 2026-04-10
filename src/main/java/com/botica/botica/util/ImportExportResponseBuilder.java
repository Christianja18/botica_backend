package com.botica.botica.util;

import com.botica.botica.service.importexport.ExportFileResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public final class ImportExportResponseBuilder {

    private ImportExportResponseBuilder() {
    }

    public static ResponseEntity<byte[]> build(ExportFileResult file) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, file.contentType())
                .body(file.content());
    }
}
