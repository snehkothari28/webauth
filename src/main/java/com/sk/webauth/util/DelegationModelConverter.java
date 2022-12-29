package com.sk.webauth.util;

import com.sk.webauth.dao.DelegationTable;
import com.sk.webauth.model.DelegationTableModel;
import jakarta.validation.Valid;

import java.util.*;

public class DelegationModelConverter {

    public static List<DelegationTableModel> getDelegatedUserModel(Set<@Valid DelegationTable> delegationTableList) {
        Map<String, DelegationTableModel> delegationTableModels = new HashMap<>();
        for (DelegationTable delegationTable : delegationTableList) {
            String email = delegationTable.getEmail();
            DelegationTableModel delegationTableModel = new DelegationTableModel(email, delegationTable.getIsWriteUser());
            if (delegationTableModels.containsKey(email)) {
                delegationTableModel.setIsWriteUser(delegationTableModels.get(email).getIsWriteUser() || delegationTable.getIsWriteUser());
            }
            delegationTableModels.put(email, delegationTableModel);
        }
        return new ArrayList<>(delegationTableModels.values());
    }

}
