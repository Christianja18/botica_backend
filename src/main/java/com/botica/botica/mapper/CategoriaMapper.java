package com.botica.botica.mapper;

import com.botica.botica.dto.CategoriaDTO;
import com.botica.botica.entity.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public CategoriaDTO toDTO(Categoria categoria) {
        if (categoria == null) {
            return null;
        }

        CategoriaDTO dto = new CategoriaDTO();
        dto.setIdCategoria(categoria.getIdCategoria());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());
        return dto;
    }

    public Categoria toEntity(CategoriaDTO dto) {
        if (dto == null) {
            return null;
        }

        Categoria categoria = new Categoria();
        categoria.setIdCategoria(dto.getIdCategoria());
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        return categoria;
    }

    public Categoria updateEntity(CategoriaDTO dto, Categoria categoria) {
        if (dto == null) {
            return categoria;
        }

        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        return categoria;
    }
}
