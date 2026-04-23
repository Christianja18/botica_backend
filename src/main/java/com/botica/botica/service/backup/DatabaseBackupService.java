package com.botica.botica.service.backup;

import com.botica.botica.service.importexport.ExportFileResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class DatabaseBackupService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupService.class);
    private static final String CONTENT_TYPE = "application/sql";
    private static final String NEW_LINE = "\n";
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Pattern DEFINER_PATTERN = Pattern.compile("\\s+DEFINER=`[^`]+`@`[^`]+`", Pattern.CASE_INSENSITIVE);

    private final DataSource dataSource;
    private final Path storageDirectory;
    private final ZoneId backupZoneId;
    private final SchemaScriptDefinitionService schemaScriptDefinitionService;

    public DatabaseBackupService(DataSource dataSource,
                                 SchemaScriptDefinitionService schemaScriptDefinitionService,
                                 @Value("${botica.backup.storage-directory:backups}") String storageDirectory,
                                 @Value("${botica.backup.timezone:America/Lima}") String timezone) {
        this.dataSource = dataSource;
        this.schemaScriptDefinitionService = schemaScriptDefinitionService;
        this.storageDirectory = Paths.get(storageDirectory);
        this.backupZoneId = ZoneId.of(timezone);
    }

    public DatabaseBackupResult createManualFullBackup() {
        return toResult(generateAndStore(DatabaseBackupType.FULL), DatabaseBackupType.FULL);
    }

    public DatabaseBackupResult createManualInsertsBackup() {
        return toResult(generateAndStore(DatabaseBackupType.INSERTS_ONLY), DatabaseBackupType.INSERTS_ONLY);
    }

    public Path createScheduledFullBackup() {
        return generateAndStore(DatabaseBackupType.FULL).storedPath();
    }

    public Path createScheduledInsertsBackup() {
        return generateAndStore(DatabaseBackupType.INSERTS_ONLY).storedPath();
    }

    private BackupExecution generateAndStore(DatabaseBackupType type) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            String schemaName = resolveSchemaName(connection);
            String content = buildBackupContent(connection, schemaName, type);
            ExportFileResult exportFile = toExportFile(schemaName, type, content);
            Path storedPath = storeFile(exportFile);
            logger.info("Backup {} generado en {}", type.description().toLowerCase(Locale.ROOT), storedPath.toAbsolutePath());
            return new BackupExecution(exportFile, storedPath);
        } catch (SQLException | IOException ex) {
            throw new IllegalStateException("No se pudo generar el respaldo de la base de datos", ex);
        }
    }

    private DatabaseBackupResult toResult(BackupExecution execution, DatabaseBackupType type) {
        return new DatabaseBackupResult(
                type.endpointSegment(),
                execution.exportFileResult().fileName(),
                execution.storedPath().toAbsolutePath().toString(),
                execution.exportFileResult().content().length,
                "Backup generado correctamente en C:\\copia"
        );
    }

    private String buildBackupContent(Connection connection, String schemaName, DatabaseBackupType type) throws SQLException {
        List<String> tables = loadBaseTables(connection, schemaName);
        List<String> orderedTables = orderTablesByDependencies(connection, tables);
        List<String> views = loadViews(connection, schemaName);
        List<String> triggers = loadTriggers(connection, schemaName);
        SchemaScriptDefinitionService.SchemaScriptDefinition schemaScriptDefinition =
                schemaScriptDefinitionService.loadDefinition().orElse(null);
        List<String> structureTableOrder = resolveObjectOrder(
                orderedTables,
                schemaScriptDefinition != null ? schemaScriptDefinition.orderedTables() : List.of()
        );
        List<String> structureViewOrder = resolveObjectOrder(
                views,
                schemaScriptDefinition != null ? schemaScriptDefinition.orderedViews() : List.of()
        );
        List<String> structureTriggerOrder = resolveObjectOrder(
                triggers,
                schemaScriptDefinition != null ? schemaScriptDefinition.orderedTriggers() : List.of()
        );

        StringBuilder builder = new StringBuilder();
        appendHeader(builder, schemaName, type);

        if (type == DatabaseBackupType.FULL) {
            appendDatabaseBootstrap(builder, schemaName);
            appendCleanupStatements(builder, schemaName, structureTableOrder, structureViewOrder, structureTriggerOrder);
            appendTableDefinitions(builder, connection, structureTableOrder, schemaScriptDefinition);
        } else {
            appendSchemaReference(builder, schemaName);
        }

        appendInsertStatements(builder, connection, schemaName, orderedTables, type);

        if (type == DatabaseBackupType.FULL) {
            appendViewDefinitions(builder, connection, structureViewOrder, schemaScriptDefinition);
            appendTriggerDefinitions(builder, connection, schemaName, structureTriggerOrder, schemaScriptDefinition);
        }

        return builder.toString();
    }

    private void appendHeader(StringBuilder builder, String schemaName, DatabaseBackupType type) {
        builder.append("-- ").append(type.description()).append(NEW_LINE);
        builder.append("-- Base de datos: ").append(schemaName).append(NEW_LINE);
        builder.append("-- Generado en: ")
                .append(ZonedDateTime.now(backupZoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .append(NEW_LINE);
        if (type == DatabaseBackupType.FULL) {
            builder.append("-- Incluye estructura de tablas, datos, vistas y triggers.").append(NEW_LINE);
            builder.append("-- Los triggers se crean al final para evitar efectos secundarios durante la restauracion.").append(NEW_LINE);
        } else {
            builder.append("-- Requiere que la estructura ya exista en el esquema destino.").append(NEW_LINE);
        }
        builder.append(NEW_LINE);
    }

    private void appendDatabaseBootstrap(StringBuilder builder, String schemaName) {
        builder.append("CREATE DATABASE IF NOT EXISTS ").append(quoteIdentifier(schemaName))
                .append(" CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;").append(NEW_LINE);
        builder.append("USE ").append(quoteIdentifier(schemaName)).append(";").append(NEW_LINE).append(NEW_LINE);
    }

    private void appendSchemaReference(StringBuilder builder, String schemaName) {
        builder.append("USE ").append(quoteIdentifier(schemaName)).append(";").append(NEW_LINE).append(NEW_LINE);
    }

    private void appendCleanupStatements(StringBuilder builder,
                                         String schemaName,
                                         List<String> orderedTables,
                                         List<String> views,
                                         List<String> triggers) {
        builder.append("-- Limpieza previa para restaurar sobre una base existente").append(NEW_LINE).append(NEW_LINE);
        builder.append("USE ").append(quoteIdentifier(schemaName)).append(";").append(NEW_LINE);
        builder.append("SET FOREIGN_KEY_CHECKS = 0;").append(NEW_LINE);
        builder.append("SET UNIQUE_CHECKS = 0;").append(NEW_LINE).append(NEW_LINE);

        for (String trigger : triggers) {
            builder.append("DROP TRIGGER IF EXISTS ")
                    .append(quoteIdentifier(schemaName))
                    .append(".")
                    .append(quoteIdentifier(trigger))
                    .append(";")
                    .append(NEW_LINE);
        }
        if (!triggers.isEmpty()) {
            builder.append(NEW_LINE);
        }

        for (String view : views) {
            builder.append("DROP VIEW IF EXISTS ").append(quoteIdentifier(view)).append(";").append(NEW_LINE);
        }
        if (!views.isEmpty()) {
            builder.append(NEW_LINE);
        }

        for (int index = orderedTables.size() - 1; index >= 0; index--) {
            String table = orderedTables.get(index);
            builder.append("DROP TABLE IF EXISTS ").append(quoteIdentifier(table)).append(";").append(NEW_LINE);
        }

        builder.append(NEW_LINE);
        builder.append("SET UNIQUE_CHECKS = 1;").append(NEW_LINE);
        builder.append("SET FOREIGN_KEY_CHECKS = 1;").append(NEW_LINE).append(NEW_LINE);
    }

    private void appendTableDefinitions(StringBuilder builder,
                                        Connection connection,
                                        List<String> orderedTables,
                                        SchemaScriptDefinitionService.SchemaScriptDefinition schemaScriptDefinition) throws SQLException {
        builder.append("-- Tablas").append(NEW_LINE).append(NEW_LINE);
        for (String table : orderedTables) {
            builder.append("-- Tabla: ").append(table).append(NEW_LINE);
            builder.append(ensureEndsWithSemicolon(resolveTableDefinition(connection, table, schemaScriptDefinition)));
            builder.append(NEW_LINE).append(NEW_LINE);
        }
    }

    private void appendInsertStatements(StringBuilder builder,
                                        Connection connection,
                                        String schemaName,
                                        List<String> orderedTables,
                                        DatabaseBackupType type) throws SQLException {
        builder.append("-- Datos").append(NEW_LINE).append(NEW_LINE);
        builder.append("SET FOREIGN_KEY_CHECKS = 0;").append(NEW_LINE);
        builder.append("SET UNIQUE_CHECKS = 0;").append(NEW_LINE).append(NEW_LINE);

        for (String table : orderedTables) {
            appendInsertStatementsForTable(builder, connection, schemaName, table, type);
        }

        builder.append("SET UNIQUE_CHECKS = 1;").append(NEW_LINE);
        builder.append("SET FOREIGN_KEY_CHECKS = 1;").append(NEW_LINE).append(NEW_LINE);
    }

    private void appendInsertStatementsForTable(StringBuilder builder,
                                                Connection connection,
                                                String schemaName,
                                                String table,
                                                DatabaseBackupType type) throws SQLException {
        List<String> insertableColumns = loadInsertableColumns(connection, schemaName, table);
        List<String> primaryKeyColumns = loadPrimaryKeyColumns(connection, table);
        builder.append("-- Inserts de ").append(table).append(NEW_LINE);

        if (insertableColumns.isEmpty()) {
            builder.append("-- No hay columnas insertables en ").append(table).append(NEW_LINE).append(NEW_LINE);
            return;
        }

        String selectSql = "SELECT " + joinQuotedIdentifiers(insertableColumns) + " FROM " + quoteIdentifier(table);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectSql)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            List<String> rows = new ArrayList<>();
            while (resultSet.next()) {
                List<String> values = new ArrayList<>();
                for (int index = 1; index <= metaData.getColumnCount(); index++) {
                    values.add(toSqlLiteral(resultSet.getObject(index)));
                }
                rows.add("(" + String.join(", ", values) + ")");
            }

            if (rows.isEmpty()) {
                builder.append("-- Sin registros").append(NEW_LINE).append(NEW_LINE);
                return;
            }

            builder.append("INSERT INTO ").append(quoteIdentifier(table))
                    .append(" (").append(joinQuotedIdentifiers(insertableColumns)).append(")")
                    .append(NEW_LINE)
                    .append("VALUES").append(NEW_LINE)
                    .append(String.join("," + NEW_LINE, rows));

            String upsertClause = buildUpsertClause(insertableColumns, primaryKeyColumns, type);
            if (!upsertClause.isBlank()) {
                builder.append(NEW_LINE).append(upsertClause);
            }

            builder.append(";")
                    .append(NEW_LINE).append(NEW_LINE);
        }
    }

    private String buildUpsertClause(List<String> insertableColumns,
                                     List<String> primaryKeyColumns,
                                     DatabaseBackupType type) {
        if (type != DatabaseBackupType.INSERTS_ONLY) {
            return "";
        }

        List<String> updatableColumns = insertableColumns.stream()
                .filter(column -> !primaryKeyColumns.contains(column))
                .toList();

        if (updatableColumns.isEmpty()) {
            return "ON DUPLICATE KEY UPDATE " + quoteIdentifier(primaryKeyColumns.get(0))
                    + " = VALUES(" + quoteIdentifier(primaryKeyColumns.get(0)) + ")";
        }

        return "ON DUPLICATE KEY UPDATE " + updatableColumns.stream()
                .map(column -> quoteIdentifier(column) + " = VALUES(" + quoteIdentifier(column) + ")")
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private void appendViewDefinitions(StringBuilder builder,
                                       Connection connection,
                                       List<String> views,
                                       SchemaScriptDefinitionService.SchemaScriptDefinition schemaScriptDefinition) throws SQLException {
        if (views.isEmpty()) {
            return;
        }

        builder.append("-- Vistas").append(NEW_LINE).append(NEW_LINE);
        for (String view : views) {
            builder.append("-- Vista: ").append(view).append(NEW_LINE);
            builder.append(ensureEndsWithSemicolon(resolveViewDefinition(connection, view, schemaScriptDefinition)));
            builder.append(NEW_LINE).append(NEW_LINE);
        }
    }

    private void appendTriggerDefinitions(StringBuilder builder,
                                          Connection connection,
                                          String schemaName,
                                          List<String> triggers,
                                          SchemaScriptDefinitionService.SchemaScriptDefinition schemaScriptDefinition) throws SQLException {
        if (triggers.isEmpty()) {
            return;
        }

        builder.append("-- Triggers").append(NEW_LINE).append(NEW_LINE);
        for (String trigger : triggers) {
            builder.append("-- Trigger: ").append(trigger).append(NEW_LINE);
            builder.append("DELIMITER $$").append(NEW_LINE);
            builder.append(ensureEndsWithCustomDelimiter(
                    resolveTriggerDefinition(connection, schemaName, trigger, schemaScriptDefinition),
                    "$$"
            ));
            builder.append(NEW_LINE);
            builder.append("DELIMITER ;").append(NEW_LINE).append(NEW_LINE);
        }
    }

    private String resolveTableDefinition(Connection connection,
                                          String table,
                                          SchemaScriptDefinitionService.SchemaScriptDefinition schemaScriptDefinition) throws SQLException {
        if (schemaScriptDefinition != null) {
            Optional<String> definition = schemaScriptDefinition.tableDefinition(table);
            if (definition.isPresent()) {
                return definition.get();
            }
        }
        return loadCreateStatement(connection, "TABLE", table);
    }

    private String resolveViewDefinition(Connection connection,
                                         String view,
                                         SchemaScriptDefinitionService.SchemaScriptDefinition schemaScriptDefinition) throws SQLException {
        if (schemaScriptDefinition != null) {
            Optional<String> definition = schemaScriptDefinition.viewDefinition(view);
            if (definition.isPresent()) {
                return definition.get();
            }
        }
        return sanitizeDefiner(loadCreateStatement(connection, "VIEW", view));
    }

    private String resolveTriggerDefinition(Connection connection,
                                            String schemaName,
                                            String trigger,
                                            SchemaScriptDefinitionService.SchemaScriptDefinition schemaScriptDefinition) throws SQLException {
        if (schemaScriptDefinition != null) {
            Optional<String> definition = schemaScriptDefinition.triggerDefinition(trigger);
            if (definition.isPresent()) {
                return definition.get();
            }
        }
        return loadCreateTriggerStatement(connection, schemaName, trigger);
    }

    private List<String> resolveObjectOrder(List<String> liveObjectNames, List<String> scriptObjectNames) {
        List<String> orderedNames = new ArrayList<>();

        for (String scriptObjectName : scriptObjectNames) {
            if (liveObjectNames.contains(scriptObjectName) && !orderedNames.contains(scriptObjectName)) {
                orderedNames.add(scriptObjectName);
            }
        }

        for (String liveObjectName : liveObjectNames) {
            if (!orderedNames.contains(liveObjectName)) {
                orderedNames.add(liveObjectName);
            }
        }

        return orderedNames;
    }

    private String resolveSchemaName(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT DATABASE()");
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next() || resultSet.getString(1) == null || resultSet.getString(1).isBlank()) {
                throw new IllegalStateException("No se pudo resolver el esquema activo de la conexion");
            }
            return resultSet.getString(1);
        }
    }

    private List<String> loadBaseTables(Connection connection, String schemaName) throws SQLException {
        return loadTableObjects(connection, schemaName, "BASE TABLE");
    }

    private List<String> loadViews(Connection connection, String schemaName) throws SQLException {
        return loadTableObjects(connection, schemaName, "VIEW");
    }

    private List<String> loadTableObjects(Connection connection, String schemaName, String tableType) throws SQLException {
        String sql = """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = ?
                  AND table_type = ?
                ORDER BY table_name
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schemaName);
            statement.setString(2, tableType);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<String> names = new ArrayList<>();
                while (resultSet.next()) {
                    names.add(resultSet.getString("table_name"));
                }
                return names;
            }
        }
    }

    private List<String> loadTriggers(Connection connection, String schemaName) throws SQLException {
        String sql = """
                SELECT trigger_name
                FROM information_schema.triggers
                WHERE trigger_schema = ?
                ORDER BY event_object_table, action_timing, event_manipulation, trigger_name
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schemaName);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<String> triggerNames = new ArrayList<>();
                while (resultSet.next()) {
                    triggerNames.add(resultSet.getString("trigger_name"));
                }
                return triggerNames;
            }
        }
    }

    private List<String> loadInsertableColumns(Connection connection, String schemaName, String table) throws SQLException {
        String sql = """
                SELECT column_name
                FROM information_schema.columns
                WHERE table_schema = ?
                  AND table_name = ?
                  AND (generation_expression IS NULL OR generation_expression = '')
                ORDER BY ordinal_position
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schemaName);
            statement.setString(2, table);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<String> columns = new ArrayList<>();
                while (resultSet.next()) {
                    columns.add(resultSet.getString("column_name"));
                }
                return columns;
            }
        }
    }

    private List<String> loadPrimaryKeyColumns(Connection connection, String table) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        Map<Short, String> orderedKeyColumns = new LinkedHashMap<>();

        try (ResultSet resultSet = metaData.getPrimaryKeys(catalog, null, table)) {
            while (resultSet.next()) {
                orderedKeyColumns.put(resultSet.getShort("KEY_SEQ"), resultSet.getString("COLUMN_NAME"));
            }
        }

        return orderedKeyColumns.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();
    }

    private List<String> orderTablesByDependencies(Connection connection, List<String> tables) throws SQLException {
        Map<String, Integer> incomingEdges = new LinkedHashMap<>();
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        Set<String> tableSet = new LinkedHashSet<>(tables);

        for (String table : tables) {
            incomingEdges.put(table, 0);
            graph.put(table, new LinkedHashSet<>());
        }

        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();

        for (String table : tables) {
            try (ResultSet resultSet = metaData.getImportedKeys(catalog, null, table)) {
                while (resultSet.next()) {
                    String dependency = resultSet.getString("PKTABLE_NAME");
                    if (!tableSet.contains(dependency) || dependency.equals(table)) {
                        continue;
                    }
                    if (graph.get(dependency).add(table)) {
                        incomingEdges.put(table, incomingEdges.get(table) + 1);
                    }
                }
            }
        }

        Queue<String> ready = new ArrayDeque<>(
                incomingEdges.entrySet().stream()
                        .filter(entry -> entry.getValue() == 0)
                        .map(Map.Entry::getKey)
                        .sorted()
                        .toList()
        );

        List<String> ordered = new ArrayList<>();
        while (!ready.isEmpty()) {
            String current = ready.poll();
            ordered.add(current);

            List<String> dependents = graph.get(current).stream()
                    .sorted()
                    .toList();
            for (String dependent : dependents) {
                int remainingDependencies = incomingEdges.get(dependent) - 1;
                incomingEdges.put(dependent, remainingDependencies);
                if (remainingDependencies == 0) {
                    ready.offer(dependent);
                }
            }
        }

        if (ordered.size() != tables.size()) {
            List<String> unresolved = tables.stream()
                    .filter(table -> !ordered.contains(table))
                    .sorted(Comparator.naturalOrder())
                    .toList();
            logger.warn("No se pudo resolver por completo el orden de dependencias de tablas. Se anexaran al final: {}", unresolved);
            ordered.addAll(unresolved);
        }

        return ordered;
    }

    private String loadCreateStatement(Connection connection, String objectType, String objectName) throws SQLException {
        String sql = "SHOW CREATE " + objectType + " " + quoteIdentifier(objectName);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (!resultSet.next()) {
                throw new IllegalStateException("No se pudo obtener la definicion para " + objectType + " " + objectName);
            }
            return resultSet.getString(2);
        }
    }

    private String loadCreateTriggerStatement(Connection connection, String schemaName, String triggerName) throws SQLException {
        String sql = "SHOW CREATE TRIGGER " + quoteIdentifier(schemaName) + "." + quoteIdentifier(triggerName);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (!resultSet.next()) {
                throw new IllegalStateException("No se pudo obtener la definicion del trigger " + triggerName);
            }
            return sanitizeDefiner(resultSet.getString(3));
        }
    }

    private ExportFileResult toExportFile(String schemaName, DatabaseBackupType type, String content) {
        String timestamp = ZonedDateTime.now(backupZoneId).format(FILE_NAME_FORMATTER);
        String fileName = schemaName + "-" + type.fileSuffix() + "-" + timestamp + ".sql";
        return new ExportFileResult(fileName, CONTENT_TYPE, content.getBytes(StandardCharsets.UTF_8));
    }

    private Path storeFile(ExportFileResult file) throws IOException {
        Files.createDirectories(storageDirectory);
        Path output = storageDirectory.resolve(file.fileName());
        Files.write(output, file.content());
        return output;
    }

    private String ensureEndsWithSemicolon(String statement) {
        String trimmed = statement.stripTrailing();
        return trimmed.endsWith(";") ? trimmed : trimmed + ";";
    }

    private String ensureEndsWithCustomDelimiter(String statement, String delimiter) {
        String trimmed = statement.stripTrailing();
        return trimmed.endsWith(delimiter) ? trimmed : trimmed + delimiter;
    }

    private String sanitizeDefiner(String statement) {
        return DEFINER_PATTERN.matcher(statement).replaceFirst("");
    }

    private String joinQuotedIdentifiers(List<String> identifiers) {
        return identifiers.stream()
                .map(this::quoteIdentifier)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private String quoteIdentifier(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    private String toSqlLiteral(Object value) throws SQLException {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof String text) {
            return quoteString(text);
        }
        if (value instanceof Character character) {
            return quoteString(character.toString());
        }
        if (value instanceof Boolean bool) {
            return bool ? "1" : "0";
        }
        if (value instanceof BigDecimal) {
            return value.toString();
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            return value.toString();
        }
        if (value instanceof Float || value instanceof Double) {
            return value.toString();
        }
        if (value instanceof Timestamp timestamp) {
            return quoteString(timestamp.toLocalDateTime().format(DATE_TIME_FORMATTER));
        }
        if (value instanceof LocalDateTime localDateTime) {
            return quoteString(localDateTime.format(DATE_TIME_FORMATTER));
        }
        if (value instanceof java.sql.Date sqlDate) {
            return quoteString(sqlDate.toLocalDate().format(DATE_FORMATTER));
        }
        if (value instanceof LocalDate localDate) {
            return quoteString(localDate.format(DATE_FORMATTER));
        }
        if (value instanceof Time time) {
            return quoteString(time.toLocalTime().format(TIME_FORMATTER));
        }
        if (value instanceof LocalTime localTime) {
            return quoteString(localTime.format(TIME_FORMATTER));
        }
        if (value instanceof byte[] bytes) {
            return "X'" + toHex(bytes) + "'";
        }
        if (value instanceof Clob clob) {
            return quoteString(clob.getSubString(1, (int) clob.length()));
        }
        return quoteString(value.toString());
    }

    private String quoteString(String value) {
        return "'" + value
                .replace("\\", "\\\\")
                .replace("'", "''")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\u0000", "\\0") + "'";
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02X", value & 0xFF));
        }
        return builder.toString();
    }

    private record BackupExecution(ExportFileResult exportFileResult, Path storedPath) {
    }
}
