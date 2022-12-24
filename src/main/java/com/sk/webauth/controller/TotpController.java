package com.sk.webauth.controller;


import com.sk.webauth.model.GeneratedSecretKeyModel;
import com.sk.webauth.model.SecretKeyModel;
import com.sk.webauth.service.TOTPCreatorService;
import com.sk.webauth.service.TOTPGeneratorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.InvalidKeyException;
import java.util.List;
import java.util.Optional;

@RestController
public class TotpController {
    private static final Logger logger = LoggerFactory.getLogger(TotpController.class);
    @Autowired
    private TOTPCreatorService totpCreatorService;

    @Autowired
    private TOTPGeneratorService totpGeneratorService;

    @PostMapping("/createAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public void createAuthorization(@Valid @RequestBody SecretKeyModel secretKeyModel, @RequestHeader("owner-email") String owner) {

        if (!StringUtils.hasLength(secretKeyModel.getSecretKey()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Secret key Missing");
        logger.info("Received request at /createAuth : {} by owner {}", secretKeyModel, owner);
        totpCreatorService.addAuth(secretKeyModel, owner);
    }

    @GetMapping("/getAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<GeneratedSecretKeyModel>> getAllSecrets(@RequestHeader("owner-email") String owner) throws InvalidKeyException {
        List<GeneratedSecretKeyModel> generatedSecretKeyModelList = totpGeneratorService.getOTPAll(owner);

        logger.info("Received request at /getAll : {} by owner {}", generatedSecretKeyModelList, owner);

        return new ResponseEntity<>(generatedSecretKeyModelList, HttpStatus.OK);

    }

    @GetMapping("/get/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GeneratedSecretKeyModel> getById(@PathVariable("id") String id, @RequestHeader("owner-email") String owner) throws InvalidKeyException {
        Optional<GeneratedSecretKeyModel> generatedSecretKeyModelOptional = totpGeneratorService.getSecretKeyById(Integer.valueOf(id), owner);
        logger.info("Received request at /get/ {} by owner {}", id, owner);

        if (generatedSecretKeyModelOptional.isEmpty()) {
            logger.error("Id {} is not present in DB", id);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        logger.info("Received request at /get/ {} : {}", id, generatedSecretKeyModelOptional.get());
        return new ResponseEntity<>(generatedSecretKeyModelOptional.get(), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public void update(@PathVariable("id") String id, @Valid @RequestBody SecretKeyModel secretKeyModel, @RequestHeader("owner-email") String owner) throws InvalidKeyException {
        if (!StringUtils.hasLength(id)) throw new NullPointerException("Id is null");
        logger.info("Received request at /update/ {} : {} by email {}", id, secretKeyModel, owner);

        totpGeneratorService.updateTOTP(Integer.valueOf(id), secretKeyModel, owner);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") String id, @RequestHeader("owner-email") String owner) {
        logger.info("Received request at /delete/ {} by email {}", id, owner);
        totpGeneratorService.deleteTOTP(Integer.valueOf(id), owner);
    }

}
