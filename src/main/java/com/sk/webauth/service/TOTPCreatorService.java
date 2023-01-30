package com.sk.webauth.service;

import com.sk.webauth.dao.DelegationTable;
import com.sk.webauth.dao.SecretKey;
import com.sk.webauth.model.DelegationTableModel;
import com.sk.webauth.model.SecretKeyModel;
import com.sk.webauth.repository.DelegationTableRepository;
import com.sk.webauth.repository.SecretKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TOTPCreatorService {
    private static final Logger logger = LoggerFactory.getLogger(TOTPCreatorService.class);
    @Autowired
    private SecretKeyRepository secretKeyRepository;

    @Autowired
    private DelegationTableRepository delegationTableRepository;

    @Value("#{'${web.auth.super.admins}'.toLowerCase().split(',')}")
    private List<String> superAdmins;

    public void addAuth(SecretKeyModel secretKeyModel, String owner, String requestId) {

        SecretKey secretKey = new SecretKey();
        secretKey.setName(secretKeyModel.getName());
        secretKey.setSecretKey(secretKeyModel.getSecretKey());
        secretKey.setType(secretKeyModel.getType());
        secretKey.setOwner(owner);
        secretKey.setUrl(secretKeyModel.getUrl());
        secretKey.setEmail(secretKeyModel.getEmail());
        secretKey.setPassword(secretKeyModel.getPassword());
        Set<DelegationTable> delegationTables = addToDelegationTable(new HashSet<>(), secretKeyModel.getDelegationTableModel(), secretKey, true);

        secretKey.setDelegationTableSet(delegationTables);
        secretKeyRepository.save(secretKey);
        delegationTableRepository.saveAll(delegationTables);
        logger.info("saved {} with id {} in DB by owner {} for requestId: {}", secretKey, secretKey.getId(), owner, requestId);
    }

    public void updateTOTP(Integer id, SecretKeyModel secretKeyModel, String owner, String requestId) {

        logger.info("email {} accessed id {} for request id {}", owner, id, requestId);
        Optional<SecretKey> secretKeyDAOOptional = secretKeyRepository.findById(id);

        if (secretKeyDAOOptional.isEmpty()) {
            logger.error("Id {} requested by owner {} not found for request id {}", id, owner, requestId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id + " not found");
        }
        SecretKey secretKey = secretKeyDAOOptional.get();
        boolean isWriteUser = false;
        if (secretKeyModel.getDelegationTableModel() != null)
            isWriteUser = secretKeyModel.getDelegationTableModel().parallelStream().filter(e -> e.getEmail().equalsIgnoreCase(owner)).anyMatch(DelegationTableModel::getIsWriteUser);
        if (!secretKey.getOwner().equalsIgnoreCase(owner) && !isWriteUser && !superAdmins.contains(owner.toLowerCase())) {
            logger.error("email {} not authorized to update write users {} for request id {}", owner, id, requestId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user " + owner + " not allowed to update  " + secretKey.getName());
        }

        secretKey.setName(secretKeyModel.getName());
        secretKey.setUrl(secretKeyModel.getUrl());
        secretKey.setType(secretKeyModel.getType());
        secretKey.setEmail(secretKeyModel.getEmail());
        secretKey.setPassword(secretKeyModel.getPassword());
        Set<DelegationTable> delegationTables;
        if (secretKey.getOwner().equalsIgnoreCase(owner) || superAdmins.contains(owner.toLowerCase())) {
            delegationTables = addToDelegationTable(secretKey.getDelegationTableSet(), secretKeyModel.getDelegationTableModel(), secretKey, true);

            logger.info("owner {} accessed id {} for request id {}", owner, id, requestId);
        } else {
            delegationTables = addToDelegationTable(secretKey.getDelegationTableSet(), secretKeyModel.getDelegationTableModel(), secretKey, false);

            logger.info("write user {} accessed id {} for request id {}", owner, id, requestId);
        }

        secretKey.setDelegationTableSet(delegationTables);
        secretKeyRepository.save(secretKey);
//        delegationTableRepository.saveAll(delegationTables);
        logger.info("updated {} with id {} in DB by owner {} for requestId: {}", secretKey, secretKey.getId(), owner, requestId);
    }

    private Set<DelegationTable> addToDelegationTable(Set<DelegationTable> delegationTableSet, List<DelegationTableModel> delegationTableModelList, SecretKey secretKey, boolean ownerUser) {

        Map<String, DelegationTableModel> modelMap = delegationTableModelList.stream().collect(Collectors.toMap(DelegationTableModel::getEmail, Function.identity()));
        Map<String, DelegationTable> tableMap = delegationTableSet.stream().collect(Collectors.toMap(DelegationTable::getEmail, Function.identity()));
        List<DelegationTable> toAdd = modelToTable(modelMap.entrySet().stream()
                .filter(e -> !tableMap.containsKey(e.getKey())).map(Map.Entry::getValue).toList(), secretKey, ownerUser);
        if (!ownerUser) {
            delegationTableSet.addAll(toAdd);
            return delegationTableSet;
        }

        List<DelegationTable> toRemove = tableMap.entrySet().stream().filter(e -> !modelMap.containsKey(e.getKey())).map(Map.Entry::getValue).toList();

        List<DelegationTable> toUpdate = updateTable(modelMap.entrySet().stream()
                .filter(e -> tableMap.containsKey(e.getKey())).map(Map.Entry::getValue).toList(), tableMap);

        delegationTableSet.addAll(toAdd);
        toRemove.forEach(delegationTableSet::remove);
        updateTable(delegationTableSet, toUpdate);

        return delegationTableSet;
    }

    private void updateTable(Set<DelegationTable> delegationTableSet, List<DelegationTable> toUpdate) {
        for (DelegationTable delegationTable : toUpdate) {

            DelegationTable table = delegationTableSet.stream().filter(e -> e.getEmail().equalsIgnoreCase(delegationTable.getEmail())).findFirst().get();
            table.setIsWriteUser(delegationTable.getIsWriteUser());
        }
    }

    private List<DelegationTable> updateTable(List<DelegationTableModel> filteredModelList, Map<String, DelegationTable> tableMap) {
        List<DelegationTable> returnTable = new ArrayList<>();
        for (DelegationTableModel model : filteredModelList) {
            DelegationTable table = tableMap.get(model.getEmail());

            table.setIsWriteUser(model.getIsWriteUser());
            returnTable.add(table);
        }
        return returnTable;
    }

    private List<DelegationTable> modelToTable(List<DelegationTableModel> modelList, SecretKey secretKey, Boolean owner) {

        return new ArrayList<>(modelList.parallelStream().map(e -> new DelegationTable(null, e.getEmail(), hasModifyAccess(owner, e.getIsWriteUser()), secretKey)).toList());
    }

    private boolean hasModifyAccess(boolean owner, boolean permission) {
        if (owner) return permission;
        return false;
    }

}
