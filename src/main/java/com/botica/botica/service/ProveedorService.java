package com.botica.botica.service;

import com.botica.botica.entity.Proveedor;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorService.class);

    private final ProveedorRepository proveedorRepository;

    public List<Proveedor> findAll() {
        return proveedorRepository.findAll();
    }

    public Proveedor findById(Integer id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con id: " + id));
    }

    public Proveedor save(Proveedor proveedor) {
        logger.info("Guardando proveedor: nombre={}, ruc={}, email={}",
                    proveedor.getNombre(), proveedor.getRuc(), proveedor.getEmail());
        try {
            Proveedor saved = proveedorRepository.save(proveedor);
            logger.info("Proveedor guardado exitosamente con id: {}", saved.getIdProveedor());
            return saved;
        } catch (Exception e) {
            logger.error("Error al guardar proveedor: nombre={}, ruc={}, error={}",
                        proveedor.getNombre(), proveedor.getRuc(), e.getMessage());
            throw e;
        }
    }

    public void deleteById(Integer id) {
        if (!proveedorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proveedor no encontrado con id: " + id);
        }
        proveedorRepository.deleteById(id);
    }
}
