package com.sk.webauth.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
public class GeneratedSecretKeyModel {
    private Integer id;
    private String name;
    private String secret;
    private String url;

    private NewType type;

    private String email;
    private String password;

    private boolean isOwner;

    @Setter(AccessLevel.NONE)
    private List<DelegationTableModel> delegationTable;
    private boolean isWriteUser;

    public void addDelegationTable(List<DelegationTableModel> delegationTableModels) {
        if (delegationTable == null) delegationTable = new ArrayList<>();
        delegationTable.addAll(delegationTableModels);
    }
}
