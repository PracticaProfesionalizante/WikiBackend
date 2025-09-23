package com.teclab.practicas.WikiBackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "documents")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del archivo es obligatorio.")
    private String name;

    @NotBlank(message = "El tipo de documento es obligatorio.")
    private String type; // Puede ser: 'PDF', 'TEXTO', 'URL'

    @NotBlank(message = "El path del archivo es obligatorio.")
    private String path;

    @URL(message = "La URL debe ser válida.")
    @NotBlank(message = "La URL es obligatoria.")
    private String url;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @NotBlank(message = "La firma del creador es obligatoria.")
    private String createdBy;

    private String updatedBy;

    //Relación ManyToMany con la entidad Role
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_document",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Roles> roles = new HashSet<>();

    private String iconName; // Ruta al logo en el sistema de archivos


}


