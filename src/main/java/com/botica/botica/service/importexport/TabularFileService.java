package com.botica.botica.service.importexport;

import com.botica.botica.exception.BadRequestException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class TabularFileService {

    public byte[] write(List<String> headers, List<Map<String, String>> rows, TabularFileFormat format) {
        try {
            return switch (format) {
                case CSV -> writeCsv(headers, rows);
                case EXCEL -> writeExcel(headers, rows);
            };
        } catch (IOException e) {
            throw new BadRequestException("No se pudo generar el archivo " + format.label() + ": " + e.getMessage());
        }
    }

    public List<Map<String, String>> read(MultipartFile file, TabularFileFormat format) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo es obligatorio y no puede estar vacio");
        }

        try {
            return switch (format) {
                case CSV -> readCsv(file);
                case EXCEL -> readExcel(file);
            };
        } catch (IOException e) {
            throw new BadRequestException("No se pudo leer el archivo " + format.label() + ": " + e.getMessage());
        }
    }

    private byte[] writeCsv(List<String> headers, List<Map<String, String>> rows) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader(headers.toArray(String[]::new))
                     .build())) {
            for (Map<String, String> row : rows) {
                printer.printRecord(buildSpreadsheetRow(headers, row));
            }
            printer.flush();
        }
        return outputStream.toByteArray();
    }

    private byte[] writeExcel(List<String> headers, List<Map<String, String>> rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("datos");
            Row headerRow = sheet.createRow(0);
            for (int index = 0; index < headers.size(); index++) {
                headerRow.createCell(index).setCellValue(headers.get(index));
            }

            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                Row dataRow = sheet.createRow(rowIndex + 1);
                Map<String, String> row = rows.get(rowIndex);
                List<String> values = buildSpreadsheetRow(headers, row);
                for (int columnIndex = 0; columnIndex < values.size(); columnIndex++) {
                    dataRow.createCell(columnIndex).setCellValue(values.get(columnIndex));
                }
            }

            for (int index = 0; index < headers.size(); index++) {
                sheet.autoSizeColumn(index);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private List<Map<String, String>> readCsv(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            List<String> headers = normalizeHeaders(parser.getHeaderNames());
            return buildRows(headers, parser.stream()
                    .map(record -> {
                        List<String> values = new ArrayList<>(headers.size());
                        for (int index = 0; index < headers.size(); index++) {
                            values.add(record.get(index));
                        }
                        return values;
                    })
                    .toList());
        }
    }

    private List<Map<String, String>> readExcel(MultipartFile file) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return List.of();
            }

            DataFormatter formatter = new DataFormatter(Locale.US);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return List.of();
            }

            List<String> headers = new ArrayList<>();
            int headerCells = headerRow.getLastCellNum();
            for (int index = 0; index < headerCells; index++) {
                Cell cell = headerRow.getCell(index, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                headers.add(normalizeHeader(formatter.formatCellValue(cell)));
            }

            List<List<String>> rawRows = new ArrayList<>();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row currentRow = sheet.getRow(rowIndex);
                if (currentRow == null) {
                    continue;
                }

                List<String> values = new ArrayList<>(headers.size());
                for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
                    Cell cell = currentRow.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    values.add(formatter.formatCellValue(cell));
                }
                rawRows.add(values);
            }

            return buildRows(headers, rawRows);
        }
    }

    private List<String> buildSpreadsheetRow(List<String> headers, Map<String, String> row) {
        List<String> values = new ArrayList<>(headers.size());
        for (String header : headers) {
            values.add(sanitizeForSpreadsheet(row.get(header)));
        }
        return values;
    }

    private List<String> normalizeHeaders(List<String> headers) {
        return headers.stream()
                .map(this::normalizeHeader)
                .toList();
    }

    private List<Map<String, String>> buildRows(List<String> headers, List<List<String>> rawRows) {
        List<Map<String, String>> rows = new ArrayList<>();
        for (List<String> rawRow : rawRows) {
            Map<String, String> row = new LinkedHashMap<>();
            boolean hasContent = false;
            for (int index = 0; index < headers.size(); index++) {
                String value = normalizeImportedValue(rawRow.get(index));
                row.put(headers.get(index), value);
                hasContent = hasContent || !value.isBlank();
            }
            if (hasContent) {
                rows.add(row);
            }
        }
        return rows;
    }

    private String normalizeHeader(String header) {
        String normalized = Objects.toString(header, "")
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace('\uFEFF', ' ')
                .replace(" ", "_")
                .replace("-", "_");
        if (normalized.isBlank()) {
            throw new BadRequestException("El archivo contiene una cabecera vacia");
        }
        return normalized;
    }

    private String sanitizeForSpreadsheet(String value) {
        String normalized = Objects.toString(value, "");
        if (!normalized.isEmpty()) {
            char firstChar = normalized.charAt(0);
            if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@') {
                return "'" + normalized;
            }
        }
        return normalized;
    }

    private String normalizeImportedValue(String value) {
        String normalized = Objects.toString(value, "").trim();
        if (normalized.length() > 1 && normalized.charAt(0) == '\'') {
            char nextChar = normalized.charAt(1);
            if (nextChar == '=' || nextChar == '+' || nextChar == '-' || nextChar == '@') {
                return normalized.substring(1);
            }
        }
        return normalized;
    }
}
