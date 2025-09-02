package com.teclab.practicas.WikiBackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoleName name;

    @ManyToMany(mappedBy = "roles") // Indica que la relación es gestionada por la entidad `User`
    private Set<User> users = new HashSet<>();


    public enum RoleName {
        ROLE_SUPER_USER,
        ROLE_ADMIN,
        ROLE_COLLABORATOR
    }
}
