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
    /**
     * Busca todos los documentos que coincidan con el 'type' especificado.
     * @param type El valor del campo 'type' por el cual filtrar.
     * @return Una lista de documentos que cumplen el criterio.
     */
    @Query("SELECT d FROM documents d " +
            "WHERE (:type IS NULL OR d.type = :type) " +
            "AND (:folder IS NULL OR d.folder = :folder)")
    List<Document> findByTypeAndFolder(@Param("type") Document.TypeName type, @Param("folder") String folder);

    /**
     * Busca todos los documentos que tienen al menos uno de los roles de acceso especificados.
     * Utiliza DISTINCT para asegurar que cada Documento aparezca solo una vez,
     * incluso si coincide con múltiples roles en la colección.
     * * @param roleNames Colección de nombres de roles de acceso.
     * @return Lista de documentos sin duplicados.
     */
    @Query("SELECT d FROM documents d JOIN FETCH d.roles r " +
            "WHERE r.name IN :roleNames " +
            "AND (:type IS NULL OR d.type = :type) " +
            "AND (:folder IS NULL OR d.folder = :folder)")
    List<Document> findDocumentsByRoleAndTypeAndFolder(@Param("roleNames") Set<String> roleNames, @Param("type") Document.TypeName type, @Param("folder") String folder);
}