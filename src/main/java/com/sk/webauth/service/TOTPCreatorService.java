package com.sk.webauth.service;

import com.sk.webauth.dao.SecretKey;
import com.sk.webauth.model.SecretKeyModel;
import com.sk.webauth.repository.SecretKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TOTPCreatorService {
    private static final Logger logger = LoggerFactory.getLogger(TOTPCreatorService.class);
    @Autowired
    private SecretKeyRepository secretKeyRepository;

    public void addAuth(SecretKeyModel secretKeyModel, String owner) {

        SecretKey secretKey = new SecretKey();
        secretKey.setName(secretKeyModel.getName());
        secretKey.setSecretKey(secretKeyModel.getSecretKey());
        secretKey.setOwner(owner);
        secretKey.setUrl(secretKeyModel.getUrl());
        secretKey.setEmail(secretKeyModel.getEmail());
        secretKey.setPassword(secretKeyModel.getPassword());
        logger.info("saving {} with id {} in DB by owner {}", secretKey, secretKey.getId(), owner);
        secretKeyRepository.save(secretKey);
        logger.info("saved {} with id {} in DB by owner {}", secretKey, secretKey.getId(), owner);
    }




}
