package com.botica.botica.service.backup;

public record DatabaseBackupResult(
        String tipo,
        String fileName,
        String absolutePath,
        long sizeBytes,
        String message
) {
}
