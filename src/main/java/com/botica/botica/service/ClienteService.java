package com.botica.botica.service;

import com.botica.botica.entity.Cliente;
import com.botica.botica.exception.ResourceNotFoundException;
import com.botica.botica.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteRepository clienteRepository;

    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    public Cliente findById(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
    }

    public Cliente save(Cliente cliente) {
        logger.info("Guardando cliente: nombre={}, apellido={}, dni={}, email={}",
                    cliente.getNombre(), cliente.getApellido(), cliente.getDni(), cliente.getEmail());
        try {
            Cliente saved = clienteRepository.save(cliente);
            logger.info("Cliente guardado exitosamente con id: {}", saved.getIdCliente());
            return saved;
        } catch (Exception e) {
            logger.error("Error al guardar cliente: nombre={}, apellido={}, dni={}, error={}",
                        cliente.getNombre(), cliente.getApellido(), cliente.getDni(), e.getMessage());
            throw e;
        }
    }

    public void deleteById(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado con id: " + id);
        }
        clienteRepository.deleteById(id);
    }
}
