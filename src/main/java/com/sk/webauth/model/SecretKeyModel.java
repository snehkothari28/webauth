package com.sk.webauth.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SecretKeyModel {
    private Integer id;

    @NotEmpty
    @NotNull

    @Size(max = 90)
    private String name;

    @Size(max=200)
    private String secretKey;
    @Size(max=90)
    private String url;

    @Size(max=90)
    private String email;

    @Size(max =90)
    private String password;

    @NotNull
    private Type type;

    @Size(max = 20)
    private List<@Valid DelegationTableModel> delegationTableModel;
}
