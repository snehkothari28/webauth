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
public class UserAuthController {
    private static final Logger logger = LoggerFactory.getLogger(UserAuthController.class);
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

        return new ResponseEntity<List<GeneratedSecretKeyModel>>(generatedSecretKeyModelList, HttpStatus.OK);

    }

    @GetMapping("/get/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<GeneratedSecretKeyModel> getById(@RequestParam("id") String id) throws InvalidKeyException {
        Optional<GeneratedSecretKeyModel> generatedSecretKeyModel = totpGeneratorService.getOTPById(Integer.valueOf(id));

        logger.info("Received request at /get/ {} : {}", id, generatedSecretKeyModel.get());

        return new ResponseEntity<GeneratedSecretKeyModel>(generatedSecretKeyModel.get(), HttpStatus.OK);
    }

}
