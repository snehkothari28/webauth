package com.sk.webauth.dao;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "secret_key")
@Getter
@Setter
@ToString
public class SecretKeyDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "name")
    private String name;
    @Column(name = "secretKey")
    private String secretKey;

}
