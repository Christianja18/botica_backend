package com.botica.botica.service.importexport;

import com.botica.botica.dto.ImportResultDTO;

import java.util.List;
import java.util.Map;

public interface ImportExportHandler {

    String resourceKey();

    List<String> headers();

    List<Map<String, String>> exportRows();

    ImportResultDTO importRows(List<Map<String, String>> rows, TabularFileFormat format);
}
