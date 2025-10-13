package com.teclab.practicas.WikiBackend.repository;

import com.teclab.practicas.WikiBackend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    boolean existsBySlug(String slug);

    @Query("SELECT d FROM documents d " +
            "WHERE (:type IS NULL OR d.type = :type) " +
            "AND (:slugPattern IS NULL OR d.slug LIKE CONCAT(:slugPattern, '%'))")
    List<Document> findByTypeAndSlug(@Param("type") Document.TypeName type, @Param("slugPattern") String slugPattern);

    @Query("SELECT d FROM documents d LEFT JOIN FETCH d.roles r " +
            "WHERE r.name IN :roleNames " +
            "AND (:type IS NULL OR d.type = :type) " +
            "AND (status = true) " +
            "AND (:slugPattern IS NULL OR d.slug LIKE CONCAT(:slugPattern, '%'))")
    List<Document> findDocumentsByRoleAndTypeAndSlug(@Param("roleNames") Set<String> roleNames, @Param("type") Document.TypeName type, @Param("slugPattern") String slugPattern);
}