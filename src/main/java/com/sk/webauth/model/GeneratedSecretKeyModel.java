package com.sk.webauth.model;

import lombok.Data;

@Data
public class GeneratedSecretKeyModel {
    private Integer id;
    private String name;
    private String secret;
    private String url;

    private String email;
    private String password;

    private Boolean isOwner;
}
