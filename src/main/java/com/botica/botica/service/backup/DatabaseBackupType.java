package com.botica.botica.service.backup;

public enum DatabaseBackupType {
    FULL("completo", "Respaldo completo", "full"),
    INSERTS_ONLY("inserts", "Respaldo de inserts", "inserts");

    private final String endpointSegment;
    private final String description;
    private final String fileSuffix;

    DatabaseBackupType(String endpointSegment, String description, String fileSuffix) {
        this.endpointSegment = endpointSegment;
        this.description = description;
        this.fileSuffix = fileSuffix;
    }

    public String endpointSegment() {
        return endpointSegment;
    }

    public String description() {
        return description;
    }

    public String fileSuffix() {
        return fileSuffix;
    }
}
