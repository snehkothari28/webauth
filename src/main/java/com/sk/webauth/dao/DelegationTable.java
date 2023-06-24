package com.sk.webauth.dao;

import com.sk.webauth.validator.annotation.EmailConstraint;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.Objects;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class DelegationTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "email")
    @NotNull
    @NotBlank
    @EmailConstraint()
    @Length(max=90)
    private String email;
    @Column(name = "isWriteUser")
    @NotNull
    private Boolean isWriteUser;

    @ManyToOne
    @JoinColumn(name = "secretKeyId")

    private SecretKey secretKey;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DelegationTable that = (DelegationTable) o;
        return Objects.equals(email, that.email) && Objects.equals(isWriteUser, that.isWriteUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, isWriteUser);
    }

    @Override
    public String toString() {
        return "DelegationTable{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", isWriteUser=" + isWriteUser +
                ", secretKeyId=" + secretKey.getId() +
                '}';
    }
}
