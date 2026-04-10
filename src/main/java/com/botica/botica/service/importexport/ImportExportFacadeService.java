package com.botica.botica.service.importexport;

import com.botica.botica.dto.ImportResultDTO;
import com.botica.botica.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ImportExportFacadeService {

    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final TabularFileService tabularFileService;
    private final Map<String, ImportExportHandler> handlers;

    public ImportExportFacadeService(TabularFileService tabularFileService, List<ImportExportHandler> handlers) {
        this.tabularFileService = tabularFileService;
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(ImportExportHandler::resourceKey, Function.identity()));
    }

    public ExportFileResult exportData(String resourceKey, TabularFileFormat format) {
        ImportExportHandler handler = resolveHandler(resourceKey);
        byte[] content = tabularFileService.write(handler.headers(), handler.exportRows(), format);
        String fileName = resourceKey + "_" + LocalDate.now().format(FILE_DATE_FORMATTER) + "." + format.extension();
        return new ExportFileResult(fileName, format.contentType(), content);
    }

    public ImportResultDTO importData(String resourceKey, MultipartFile file, TabularFileFormat format) {
        ImportExportHandler handler = resolveHandler(resourceKey);
        List<Map<String, String>> rows = tabularFileService.read(file, format);
        return handler.importRows(rows, format);
    }

    private ImportExportHandler resolveHandler(String resourceKey) {
        ImportExportHandler handler = handlers.get(resourceKey);
        if (handler == null) {
            throw new BadRequestException("No existe un handler de importacion/exportacion para: " + resourceKey);
        }
        return handler;
    }
}
