package com.botica.botica.repository;

import com.botica.botica.entity.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Integer> {

    Optional<Boleta> findByNumeroBoleta(String numeroBoleta);
}
