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

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
public class TotpController {
    private static final Logger logger = LoggerFactory.getLogger(TotpController.class);
    @Autowired
    private TOTPCreatorService totpCreatorService;

    @Autowired
    private TOTPGeneratorService totpGeneratorService;

    @PostMapping("/createAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public void createAuthorization(@Valid @RequestBody SecretKeyModel secretKeyModel, @RequestHeader("owner-email") String owner, @RequestHeader("requestId") String requestId) {

        if (!StringUtils.hasLength(secretKeyModel.getSecretKey()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Secret key Missing");
        logger.info("Received request at /createAuth : {} by owner {} for requestId: {}", secretKeyModel, owner, requestId);
        totpCreatorService.addAuth(secretKeyModel, owner, requestId);
    }

    @GetMapping("/getAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<GeneratedSecretKeyModel>> getAllSecrets(@RequestHeader("owner-email") String owner, @RequestHeader("requestId") String requestId) {
        List<GeneratedSecretKeyModel> generatedSecretKeyModelList = totpGeneratorService.getOTPAll(owner, requestId);
        logger.info("Received request at /getAll : {} by owner {} for requestId: {}", generatedSecretKeyModelList, owner, requestId);
        return new ResponseEntity<>(generatedSecretKeyModelList, HttpStatus.OK);

    }

    @GetMapping("/get/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GeneratedSecretKeyModel> getById(@PathVariable("id") String id, @RequestHeader("owner-email") String owner, @RequestHeader("requestId") String requestId) {
        logger.info("Received request at /get/ {} by owner {}", id, owner);
        Optional<GeneratedSecretKeyModel> generatedSecretKeyModelOptional = totpGeneratorService.getSecretKeyById(Integer.valueOf(id), owner, requestId);

        if (generatedSecretKeyModelOptional.isEmpty()) {
            logger.error("Id {} is not present in DB for requestId: {}", id, requestId);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        logger.info("Received request at /get/ {} : {} for requestId: {}", id, generatedSecretKeyModelOptional.get(), requestId);
        return new ResponseEntity<>(generatedSecretKeyModelOptional.get(), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public void update(@PathVariable("id") String id, @Valid @RequestBody SecretKeyModel secretKeyModel, @RequestHeader("owner-email") String owner, @RequestHeader("requestId") String requestId) {
        if (!StringUtils.hasLength(id)) throw new NullPointerException("Id is null");
        logger.info("Received request at /update/ {} : {} by email {} for requestId: {}", id, secretKeyModel, owner, requestId);

        totpCreatorService.updateTOTP(Integer.valueOf(id), secretKeyModel, owner, requestId);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") String id, @RequestHeader("owner-email") String owner, @RequestHeader("requestId") String requestId) {
        logger.info("Received request at /delete/ {} by email {} for requestId: {}", id, owner, requestId);
        totpGeneratorService.deleteTOTP(Integer.valueOf(id), owner, requestId);
    }

    @GetMapping("/types")
    @ResponseStatus(HttpStatus.OK)
    public Set<String> getTypes(@RequestHeader("owner-email") String owner, @RequestHeader("requestId") String requestId){
        logger.info("Received request at /types by email {} for requestId: {}", owner, requestId);
        return totpGeneratorService.getTypes();
    }

    @GetMapping("/deletedRecords")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<GeneratedSecretKeyModel>> getDeletedRecords(@RequestHeader("owner-email") String owner, @RequestHeader("requestId") String requestId)
    {
        logger.info("Received request at /deletedRecords by email {} for requestId: {}", owner, requestId);
        List<GeneratedSecretKeyModel> deleted = totpGeneratorService.getDeletedRecords(owner,requestId);
        return new ResponseEntity<>(deleted,HttpStatus.OK);
    }
    @GetMapping("/restoredRecords/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void getRestoredRecords(@PathVariable("id") Integer id,@RequestHeader("owner-email") String owner, @RequestHeader("requestId") String requestId)
    {
        logger.info("Received request at /restoredRecords/{} by email {} for requestId: {}", id, owner, requestId);
        totpGeneratorService.restoreDeletedRecords(id, owner,requestId);
    }

}
