package com.sk.webauth.repository;

import com.sk.webauth.dao.SecretKeyDAO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SecretKeyRepository extends CrudRepository<SecretKeyDAO, Long> {

    List<SecretKeyDAO> findByName(String lastName);

    SecretKeyDAO findById(long id);
}
