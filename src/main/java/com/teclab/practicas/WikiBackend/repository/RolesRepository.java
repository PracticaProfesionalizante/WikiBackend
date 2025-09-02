package com.teclab.practicas.WikiBackend.repository;

import com.teclab.practicas.WikiBackend.entity.Roles;
import com.teclab.practicas.WikiBackend.entity.Roles.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface RolesRepository extends JpaRepository<Roles, Long> {
    Optional<Roles> findByName(RoleName name);


}