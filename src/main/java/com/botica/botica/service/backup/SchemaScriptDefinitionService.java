package com.botica.botica.service.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SchemaScriptDefinitionService {

    private static final Logger logger = LoggerFactory.getLogger(SchemaScriptDefinitionService.class);
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile("^CREATE\\s+TABLE\\s+`?([^`\\s(]+)`?\\s*\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern CREATE_VIEW_PATTERN = Pattern.compile("^CREATE\\s+VIEW\\s+`?([^`\\s]+)`?\\s+AS\\b.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern CREATE_TRIGGER_PATTERN = Pattern.compile("^CREATE\\s+TRIGGER\\s+`?([^`\\s]+)`?\\s+.*", Pattern.CASE_INSENSITIVE);

    private final String schemaScriptPath;

    public SchemaScriptDefinitionService(@Value("${botica.backup.schema-script-path:../scriptbotica.sql}") String schemaScriptPath) {
        this.schemaScriptPath = schemaScriptPath;
    }

    public Optional<SchemaScriptDefinition> loadDefinition() {
        Path path = resolvePath(schemaScriptPath);
        if (!Files.exists(path)) {
            logger.warn("No se encontro el script canonico de esquema en {}", path.toAbsolutePath());
            return Optional.empty();
        }

        try {
            return Optional.of(parse(Files.readAllLines(path)));
        } catch (IOException ex) {
            logger.warn("No se pudo leer el script canonico de esquema en {}", path.toAbsolutePath(), ex);
            return Optional.empty();
        }
    }

    private SchemaScriptDefinition parse(List<String> lines) {
        LinkedHashMap<String, String> tableDefinitions = new LinkedHashMap<>();
        LinkedHashMap<String, String> viewDefinitions = new LinkedHashMap<>();
        LinkedHashMap<String, String> triggerDefinitions = new LinkedHashMap<>();

        ParseMode mode = ParseMode.NONE;
        String currentObjectName = null;
        String currentTriggerDelimiter = null;
        List<String> buffer = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();

            if (mode == ParseMode.NONE) {
                if (trimmed.toUpperCase(Locale.ROOT).startsWith("DELIMITER ")) {
                    currentTriggerDelimiter = trimmed.substring("DELIMITER ".length()).trim();
                    continue;
                }

                Matcher tableMatcher = CREATE_TABLE_PATTERN.matcher(trimmed);
                if (tableMatcher.find()) {
                    mode = ParseMode.TABLE;
                    currentObjectName = tableMatcher.group(1);
                    buffer = new ArrayList<>();
                    buffer.add(line);
                    continue;
                }

                Matcher viewMatcher = CREATE_VIEW_PATTERN.matcher(trimmed);
                if (viewMatcher.find()) {
                    mode = ParseMode.VIEW;
                    currentObjectName = viewMatcher.group(1);
                    buffer = new ArrayList<>();
                    buffer.add(line);
                    if (trimmed.endsWith(";")) {
                        viewDefinitions.put(currentObjectName, join(buffer));
                        mode = ParseMode.NONE;
                    }
                    continue;
                }

                Matcher triggerMatcher = CREATE_TRIGGER_PATTERN.matcher(trimmed);
                if (triggerMatcher.find()) {
                    mode = ParseMode.TRIGGER;
                    currentObjectName = triggerMatcher.group(1);
                    buffer = new ArrayList<>();
                    buffer.add(line);
                }
                continue;
            }

            if (mode == ParseMode.TABLE) {
                buffer.add(line);
                if (trimmed.endsWith(";")) {
                    tableDefinitions.put(currentObjectName, join(buffer));
                    mode = ParseMode.NONE;
                }
                continue;
            }

            if (mode == ParseMode.VIEW) {
                buffer.add(line);
                if (trimmed.endsWith(";")) {
                    viewDefinitions.put(currentObjectName, join(buffer));
                    mode = ParseMode.NONE;
                }
                continue;
            }

            if (mode == ParseMode.TRIGGER) {
                if (currentTriggerDelimiter != null && trimmed.equals(currentTriggerDelimiter)) {
                    triggerDefinitions.put(currentObjectName, join(buffer));
                    mode = ParseMode.NONE;
                    continue;
                }

                buffer.add(line);
            }
        }

        return new SchemaScriptDefinition(tableDefinitions, viewDefinitions, triggerDefinitions);
    }

    private Path resolvePath(String rawPath) {
        Path path = Paths.get(rawPath);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return Paths.get("").toAbsolutePath().resolve(path).normalize();
    }

    private String join(List<String> lines) {
        return String.join("\n", lines);
    }

    private enum ParseMode {
        NONE,
        TABLE,
        VIEW,
        TRIGGER
    }

    public record SchemaScriptDefinition(
            LinkedHashMap<String, String> tableDefinitions,
            LinkedHashMap<String, String> viewDefinitions,
            LinkedHashMap<String, String> triggerDefinitions
    ) {
        public List<String> orderedTables() {
            return new ArrayList<>(tableDefinitions.keySet());
        }

        public List<String> orderedViews() {
            return new ArrayList<>(viewDefinitions.keySet());
        }

        public List<String> orderedTriggers() {
            return new ArrayList<>(triggerDefinitions.keySet());
        }

        public Optional<String> tableDefinition(String tableName) {
            return Optional.ofNullable(tableDefinitions.get(tableName));
        }

        public Optional<String> viewDefinition(String viewName) {
            return Optional.ofNullable(viewDefinitions.get(viewName));
        }

        public Optional<String> triggerDefinition(String triggerName) {
            return Optional.ofNullable(triggerDefinitions.get(triggerName));
        }
    }
}
