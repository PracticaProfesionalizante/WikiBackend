package com.teclab.practicas.WikiBackend.repository;

import com.teclab.practicas.WikiBackend.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    /**
     * Busca todos los ítems de menú de nivel superior asociados a los roles dados,
     * cargando de forma optimizada toda su jerarquía de hijos en una sola consulta.
     *
     * @param roleNames El conjunto de nombres de roles del usuario.
     * @return Una lista de objetos MenuItem que actúan como ítems de menú principales.
     */
    @Query("SELECT DISTINCT m FROM MenuItem m LEFT JOIN FETCH m.children c JOIN m.roles r WHERE m.parent IS NULL AND r.name IN :roleNames ORDER BY m.order ASC")
    List<MenuItem> findMainMenusByRoles(Set<String> roleNames);
}