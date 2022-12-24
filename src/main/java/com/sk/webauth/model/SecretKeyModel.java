package com.sk.webauth.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SecretKeyModel {
    private Integer id;

    @NotEmpty
    @NotNull
    private String name;

    private String secretKey;

    private String url;

    private String email;

    private String password;
}
