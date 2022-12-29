package com.sk.webauth.repository;

import com.sk.webauth.dao.DelegationTable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface DelegationTableRepository extends CrudRepository<DelegationTable, Integer> {

    List<DelegationTable> findByEmail(String email);
}
