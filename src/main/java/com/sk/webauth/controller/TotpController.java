package com.sk.webauth.controller;


import com.sk.webauth.model.GeneratedSecretKeyModel;
import com.sk.webauth.model.SecretKeyModel;
import com.sk.webauth.service.TOTPCreatorService;
import com.sk.webauth.service.TOTPGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public void createAuthorization(@RequestBody SecretKeyModel secretKeyModel) {

        logger.info("Received request at /createAuth : {}", secretKeyModel);
        totpCreatorService.addAuth(secretKeyModel);
    }

    @GetMapping("/getAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<GeneratedSecretKeyModel>> getAllSecrets() throws InvalidKeyException {
        List<GeneratedSecretKeyModel> generatedSecretKeyModelList = totpGeneratorService.getOTPAll();

        logger.info("Received request at /getAll : {}", generatedSecretKeyModelList);

        return new ResponseEntity<>(generatedSecretKeyModelList, HttpStatus.OK);

    }

    @GetMapping("/get/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SecretKeyModel> getById(@PathVariable("id") String id) throws InvalidKeyException {
        Optional<SecretKeyModel> secretKeyModel = totpGeneratorService.getSecretKeyById(Integer.valueOf(id));

        if (secretKeyModel.isEmpty()) {
            logger.error("Id {} is not present in DB", id);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        logger.info("Received request at /get/ {} : {}", id, secretKeyModel.get());
        return new ResponseEntity<>(secretKeyModel.get(), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public void update(@PathVariable("id") String id, @RequestBody SecretKeyModel secretKeyModel) throws InvalidKeyException {
        logger.info("Received request at /update/ {} : {}", id, secretKeyModel);
        totpGeneratorService.updateTOTP(Integer.valueOf(id), secretKeyModel);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") String id) throws InvalidKeyException {
        logger.info("Received request at /delete/ {}", id);
        totpGeneratorService.deleteTOTP(Integer.valueOf(id));
    }

}
