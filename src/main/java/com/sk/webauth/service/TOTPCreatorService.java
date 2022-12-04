package com.sk.webauth.service;

import com.sk.webauth.dao.SecretKeyDAO;
import com.sk.webauth.model.SecretKeyModel;
import com.sk.webauth.repository.SecretKeyRepository;
import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

@Service
public class TOTPCreatorService {
    private static final Logger logger = LoggerFactory.getLogger(TOTPCreatorService.class);
    @Autowired
    private SecretKeyRepository secretKeyRepository;

    public void addAuth(SecretKeyModel secretKeyModel) {

        SecretKeyDAO secretKeyDAO = new SecretKeyDAO();
        secretKeyDAO.setName(secretKeyModel.getName());
        secretKeyDAO.setSecretKey(secretKeyModel.getSecretKey());
        secretKeyDAO.setOwner(secretKeyModel.getOwner());
        logger.info("saving {} with id {} in DB", secretKeyDAO, secretKeyDAO.getId());
        secretKeyRepository.save(secretKeyDAO);
        logger.info("saved {} with id {} in DB", secretKeyDAO, secretKeyDAO.getId());
    }




}
