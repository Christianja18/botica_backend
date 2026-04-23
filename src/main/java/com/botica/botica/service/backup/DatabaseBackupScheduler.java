package com.botica.botica.service.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DatabaseBackupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupScheduler.class);

    private final DatabaseBackupService databaseBackupService;

    public DatabaseBackupScheduler(DatabaseBackupService databaseBackupService) {
        this.databaseBackupService = databaseBackupService;
    }

    @Scheduled(
            cron = "${botica.backup.full.cron:0 0 2 1 1,4,7,10 *}",
            zone = "${botica.backup.timezone:America/Lima}"
    )
    public void generateQuarterlyFullBackup() {
        executeScheduledBackup(DatabaseBackupType.FULL);
    }

    @Scheduled(
            cron = "${botica.backup.inserts.cron:0 30 2 1 1,4,7,10 *}",
            zone = "${botica.backup.timezone:America/Lima}"
    )
    public void generateQuarterlyInsertsBackup() {
        executeScheduledBackup(DatabaseBackupType.INSERTS_ONLY);
    }

    private void executeScheduledBackup(DatabaseBackupType type) {
        try {
            if (type == DatabaseBackupType.FULL) {
                databaseBackupService.createScheduledFullBackup();
            } else {
                databaseBackupService.createScheduledInsertsBackup();
            }
        } catch (RuntimeException ex) {
            logger.error("Fallo la ejecucion programada del backup {}", type.description().toLowerCase(), ex);
        }
    }
}
