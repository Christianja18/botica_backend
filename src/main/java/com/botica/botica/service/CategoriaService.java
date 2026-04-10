package com.botica.botica.service;

import com.botica.botica.entity.Categoria;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    public Page<Categoria> findAll(Pageable pageable) {
        return categoriaRepository.findAll(pageable);
    }

    public Categoria findById(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con id: " + id));
    }

    public Categoria save(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public void deleteById(Integer id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoria no encontrada con id: " + id);
        }
        categoriaRepository.deleteById(id);
    }
}
