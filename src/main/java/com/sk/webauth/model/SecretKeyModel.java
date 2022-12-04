package com.sk.webauth.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SecretKeyModel {
    private Integer id;

    private String name;

    private String secretKey;

    private String owner;
}
