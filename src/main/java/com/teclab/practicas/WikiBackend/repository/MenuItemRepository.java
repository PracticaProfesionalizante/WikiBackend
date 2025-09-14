package com.teclab.practicas.WikiBackend.repository;

import com.teclab.practicas.WikiBackend.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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
    @Query("SELECT DISTINCT m FROM menu_items m LEFT JOIN FETCH m.children c JOIN m.roles r WHERE m.parent IS NULL AND r.name IN :roleNames ORDER BY m.order ASC")
    List<MenuItem> findMainMenusByRoles(Set<String> roleNames);


    //(m.parent.id = :parentId OR (:parentId IS NULL AND m.parent IS NULL))
    @Modifying
    @Query("UPDATE menu_items m SET m.order = m.order + 1 WHERE (m.parent.id = :parentId OR (:parentId IS NULL AND m.parent IS NULL)) AND m.order >= :start AND m.order <= :end")
    void increaseOrder(@Param("parentId") Long parentId, @Param("start") Integer start, @Param("end") Integer end);

    @Modifying
    @Query("UPDATE menu_items m SET m.order = m.order - 1 WHERE (m.parent.id = :parentId OR (:parentId IS NULL AND m.parent IS NULL)) AND m.order >= :start AND m.order <= :end")
    void decreaseOrder(@Param("parentId") Long parentId, @Param("start") Integer start, @Param("end") Integer end);

    @Modifying
    @Query("UPDATE menu_items m SET m.order = m.order - 1 WHERE (m.parent.id = :parentId OR (:parentId IS NULL AND m.parent IS NULL)) AND m.order > :order")
    void adjustOrderOnDeleteForParentedItems(@Param("parentId") Long parentId, @Param("order") Integer order);

    @Modifying
    @Query("UPDATE menu_items m SET m.order = m.order + 1 WHERE (m.parent.id = :parentId OR (:parentId IS NULL AND m.parent IS NULL)) AND m.order >= :order")
    void adjustOrderOnChangeParent(@Param("parentId") Long parentId, @Param("order") Integer order);

    @Query("SELECT MAX(m.order) FROM menu_items m WHERE (m.parent.id = :parentId OR (:parentId IS NULL AND m.parent IS NULL))")
    Optional<Integer> findMaxOrderByParentId(@Param("parentId") Long parentId);
}