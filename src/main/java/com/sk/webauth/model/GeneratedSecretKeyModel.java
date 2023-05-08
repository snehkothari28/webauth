package com.sk.webauth.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class GeneratedSecretKeyModel {
    private Integer id;
    private String name;
    private String secret;
    private String url;

    private String email;
    private String password;

    private String type;

    private boolean isOwner;

    private Boolean deleted=false;

    private String deletedBy;

    private LocalDateTime deletedAt;

    @Setter(AccessLevel.NONE)
    private List<DelegationTableModel> delegationTable;
    private boolean isWriteUser;

    public void addDelegationTable(List<DelegationTableModel> delegationTableModels) {
        if (delegationTable == null) delegationTable = new ArrayList<>();
        delegationTable.addAll(delegationTableModels);
    }
}
