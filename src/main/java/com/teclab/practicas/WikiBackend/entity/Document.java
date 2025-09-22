package com.teclab.practicas.WikiBackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

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

    @NotBlank(message = "El nombre del enlace es obligatorio.")
    private String name;

    @URL(message = "La URL debe ser válida.")
    @NotBlank(message = "La URL es obligatoria.")
    private String url;

    private String iconName; // Ruta al logo en el sistema de archivos


}


