package com.botica.botica.controller;

import com.botica.botica.service.backup.DatabaseBackupResult;
import com.botica.botica.service.backup.DatabaseBackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backups")
@RequiredArgsConstructor
@Tag(name = "Backups", description = "Respaldo manual y programado de la base de datos")
public class DatabaseBackupController {

    private final DatabaseBackupService databaseBackupService;

    @PostMapping("/completo")
    @Operation(
            summary = "Generar backup completo manual",
            description = "Genera un script SQL con estructura de tablas, inserts, vistas y triggers y lo guarda en el disco local C."
    )
    public ResponseEntity<DatabaseBackupResult> generateFullBackup() {
        return ResponseEntity.ok(databaseBackupService.createManualFullBackup());
    }

    @PostMapping("/inserts")
    @Operation(
            summary = "Generar backup manual solo de inserts",
            description = "Genera un script SQL con solo los INSERT de las tablas y lo guarda en el disco local C."
    )
    public ResponseEntity<DatabaseBackupResult> generateInsertsBackup() {
        return ResponseEntity.ok(databaseBackupService.createManualInsertsBackup());
    }
}
