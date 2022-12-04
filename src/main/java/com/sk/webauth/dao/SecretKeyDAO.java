package com.sk.webauth.dao;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "secret_key")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecretKeyDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    private String name;

    private String secretKey;

    private String owner;
}
