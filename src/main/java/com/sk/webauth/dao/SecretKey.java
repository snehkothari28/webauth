package com.sk.webauth.dao;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class SecretKey {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "name")
    @NotNull
    @NotBlank
    private String name;
    @Column(name = "secretKey")
    @NotNull
    @NotBlank
    private String secretKey;
    @Column(name = "owner")
    @NotNull
    @NotBlank
    private String owner;

    @Column(name = "url")
    private String url;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;


}
