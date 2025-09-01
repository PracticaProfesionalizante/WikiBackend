package com.teclab.practicas.WikiBackend.repository;

import com.teclab.practicas.WikiBackend.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Método para buscar un usuario por su nombre de usuario
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(String username);

    // Método para verificar si un nombre de usuario ya existe
    boolean existsByUsername(String username);

    // Método para verificar si un email ya existe
    boolean existsByEmail(String email);
}