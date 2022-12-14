package com.sk.webauth.repository;

import com.sk.webauth.dao.SecretKeyDAO;
import org.springframework.data.repository.CrudRepository;


public interface SecretKeyRepository extends CrudRepository<SecretKeyDAO, Integer> {

}
