package com.botica.botica.service.importexport;

import com.botica.botica.dto.CategoriaDTO;
import com.botica.botica.entity.Categoria;
import com.botica.botica.exception.BadRequestException;
import com.botica.botica.repository.CategoriaRepository;
import com.botica.botica.service.CategoriaService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoriaImportExportHandler extends AbstractImportExportHandler {

    private final CategoriaService categoriaService;
    private final CategoriaRepository categoriaRepository;

    public CategoriaImportExportHandler(Validator validator,
                                        CategoriaService categoriaService,
                                        CategoriaRepository categoriaRepository) {
        super(validator);
        this.categoriaService = categoriaService;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public String resourceKey() {
        return "categorias";
    }

    @Override
    public List<String> headers() {
        return List.of("nombre", "descripcion");
    }

    @Override
    public List<Map<String, String>> exportRows() {
        return categoriaService.findAll().stream()
                .map(categoria -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("nombre", valueOf(categoria.getNombre()));
                    row.put("descripcion", valueOf(categoria.getDescripcion()));
                    return row;
                })
                .toList();
    }

    @Override
    protected ImportAction importRow(Map<String, String> row) {
        Integer idCategoria = optionalInteger(row, "id_categoria");
        String nombre = requiredString(row, "nombre");

        Categoria categoria = resolveCategoria(idCategoria, nombre);
        boolean updating = categoria != null;
        if (!updating) {
            categoria = new Categoria();
        }

        validateUniqueNombre(nombre, categoria.getIdCategoria());

        CategoriaDTO dto = new CategoriaDTO(
                categoria.getIdCategoria(),
                nombre,
                optionalString(row, "descripcion")
        );
        validate(dto);

        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoriaService.save(categoria);
        return updating ? ImportAction.UPDATED : ImportAction.INSERTED;
    }

    private Categoria resolveCategoria(Integer idCategoria, String nombre) {
        if (idCategoria != null) {
            return categoriaService.findById(idCategoria);
        }
        return categoriaRepository.findByNombreIgnoreCase(nombre).orElse(null);
    }

    private void validateUniqueNombre(String nombre, Integer currentId) {
        categoriaRepository.findByNombreIgnoreCase(nombre)
                .filter(existing -> currentId == null || !existing.getIdCategoria().equals(currentId))
                .ifPresent(existing -> {
                    throw new BadRequestException("Ya existe una categoria con el nombre " + nombre);
                });
    }
}
