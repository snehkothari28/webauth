package com.sk.webauth.dao;

import com.sk.webauth.model.Type;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class SecretKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;

    @OneToMany(mappedBy = "secretKey", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<@Valid DelegationTable> delegationTableSet;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecretKey secretKey = (SecretKey) o;
        return Objects.equals(id, secretKey.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SecretKey{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", owner='" + owner + '\'' +
                ", url='" + url + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", type='" + type + '\'' +
                ", delegationTableList=" + delegationTableSet +
                '}';
    }
}
