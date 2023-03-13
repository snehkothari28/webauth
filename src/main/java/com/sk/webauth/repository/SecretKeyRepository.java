package com.sk.webauth.repository;

import com.sk.webauth.dao.DelegationTable;
import com.sk.webauth.dao.SecretKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface SecretKeyRepository extends CrudRepository<SecretKey, Integer> {

    List<SecretKey> findByOwner(String owner);

    public static final String FIND_TYPES = "SELECT s.type FROM secret_key s";

    @Query(value = FIND_TYPES, nativeQuery = true)
    List<String> findAllType();
}
