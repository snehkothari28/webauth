package com.sk.webauth.model;

import com.sk.webauth.validator.annotation.EmailConstraint;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DelegationTableModel {
    @NotEmpty
    @NotNull
    @EmailConstraint()
    private String email;

    @NotNull
    private Boolean isWriteUser;
}
