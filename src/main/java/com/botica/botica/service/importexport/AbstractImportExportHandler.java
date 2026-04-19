package com.botica.botica.service.importexport;

import com.botica.botica.dto.ImportResultDTO;
import com.botica.botica.exception.BadRequestException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public abstract class AbstractImportExportHandler implements ImportExportHandler {

    private final Validator validator;

    protected AbstractImportExportHandler(Validator validator) {
        this.validator = validator;
    }

    @Override
    public ImportResultDTO importRows(List<Map<String, String>> rows, TabularFileFormat format) {
        int inserted = 0;
        int updated = 0;
        int failed = 0;
        List<ImportResultDTO.ImportErrorDTO> errors = new ArrayList<>();

        for (int index = 0; index < rows.size(); index++) {
            Map<String, String> row = rows.get(index);
            try {
                ImportAction action = importRow(row);
                if (action == ImportAction.INSERTED) {
                    inserted++;
                } else {
                    updated++;
                }
            } catch (Exception ex) {
                failed++;
                errors.add(ImportResultDTO.ImportErrorDTO.builder()
                        .fila(index + 2)
                        .mensaje(extractMessage(ex))
                        .build());
            }
        }

        return ImportResultDTO.builder()
                .recurso(resourceKey())
                .formato(format.label())
                .totalFilas(rows.size())
                .insertados(inserted)
                .actualizados(updated)
                .fallidos(failed)
                .errores(errors)
                .build();
    }

    protected abstract ImportAction importRow(Map<String, String> row);

    protected <T> void validate(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            StringJoiner joiner = new StringJoiner("; ");
            for (ConstraintViolation<T> violation : violations) {
                joiner.add(violation.getPropertyPath() + ": " + violation.getMessage());
            }
            throw new BadRequestException(joiner.toString());
        }
    }

    protected Integer optionalInteger(Map<String, String> row, String key) {
        String value = optionalString(row, key);
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("El campo " + key + " debe ser un numero entero");
        }
    }

    protected Integer requiredInteger(Map<String, String> row, String key) {
        Integer value = optionalInteger(row, key);
        if (value == null) {
            throw new BadRequestException("El campo " + key + " es obligatorio");
        }
        return value;
    }

    protected BigDecimal optionalDecimal(Map<String, String> row, String key) {
        String value = optionalString(row, key);
        if (value == null) {
            return null;
        }
        String normalized = value.replace(",", ".");
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("El campo " + key + " debe ser un numero decimal valido");
        }
    }

    protected LocalDateTime optionalDateTime(Map<String, String> row, String key) {
        String value = optionalString(row, key);
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ex) {
            throw new BadRequestException("El campo " + key + " debe tener un formato de fecha y hora valido");
        }
    }

    protected LocalDateTime requiredDateTime(Map<String, String> row, String key) {
        LocalDateTime value = optionalDateTime(row, key);
        if (value == null) {
            throw new BadRequestException("El campo " + key + " es obligatorio");
        }
        return value;
    }

    protected Boolean optionalBoolean(Map<String, String> row, String key) {
        String value = optionalString(row, key);
        if (value == null) {
            return null;
        }

        return switch (value.trim().toLowerCase()) {
            case "true", "1", "si", "sí", "yes", "verdadero" -> true;
            case "false", "0", "no", "falso" -> false;
            default -> throw new BadRequestException("El campo " + key + " debe ser true/false, 1/0 o si/no");
        };
    }

    protected String requiredString(Map<String, String> row, String key) {
        String value = optionalString(row, key);
        if (value == null) {
            throw new BadRequestException("El campo " + key + " es obligatorio");
        }
        return value;
    }

    protected String optionalString(Map<String, String> row, String key) {
        String value = Objects.toString(row.get(key), "").trim();
        return value.isBlank() ? null : value;
    }

    protected String extractMessage(Exception ex) {
        Throwable current = ex;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return (message == null || message.isBlank()) ? ex.getClass().getSimpleName() : message;
    }

    protected String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
