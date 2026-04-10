package com.botica.botica.repository;

import com.botica.botica.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @Override
    @EntityGraph(attributePaths = {"rol"})
    List<Usuario> findAll();

    @Override
    @EntityGraph(attributePaths = {"rol"})
    Optional<Usuario> findById(Integer id);

    @EntityGraph(attributePaths = {"rol"})
    Optional<Usuario> findByEmail(String email);

    @EntityGraph(attributePaths = {"rol"})
    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);
}
