package com.teclab.practicas.WikiBackend.dto.user;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Integer id;
    private String username;
    private String email;
    private Boolean enabled;
    private Time createdAt;    //Consultar gems
    private time updatedAt;    //Consultar gems



}
