package com.teclab.practicas.WikiBackend.repository;

import com.teclab.practicas.WikiBackend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Metodo para buscar documentos que tienen roles de acceso específicos
    List<Document> findByAccessRoles_NameIn(Collection<String> roleNames);
}