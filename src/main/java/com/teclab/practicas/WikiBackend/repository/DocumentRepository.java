package com.teclab.practicas.WikiBackend.repository;

import com.teclab.practicas.WikiBackend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
}