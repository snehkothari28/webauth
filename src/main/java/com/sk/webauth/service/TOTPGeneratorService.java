package com.sk.webauth.service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.sk.webauth.dao.SecretKey;
import com.sk.webauth.model.GeneratedSecretKeyModel;
import com.sk.webauth.model.SecretKeyModel;
import com.sk.webauth.repository.SecretKeyRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TOTPGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(TOTPGeneratorService.class);
    TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
    KeyGenerator keyGenerator;
    Base32 base32 = new Base32();
    @Value("#{'${web.auth.super.admins}'.toLowerCase().split(',')}")
    private List<String> superAdmins;
    @Autowired
    private SecretKeyRepository secretKeyRepository;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        keyGenerator = KeyGenerator.getInstance(totp.getAlgorithm());
        int macLengthInBytes = Mac.getInstance(totp.getAlgorithm()).getMacLength();
        keyGenerator.init(macLengthInBytes * 8);
    }


    public List<GeneratedSecretKeyModel> getOTPAll(String owner) {
        Iterable<SecretKey> secretKeyDAOList = secretKeyRepository.findAll();
        List<GeneratedSecretKeyModel> generatedSecretKeyModelList = new ArrayList<>();
        for (SecretKey secretKey : secretKeyDAOList) {
            generatedSecretKeyModelList.add(modelMapper(owner, secretKey));
        }
        return generatedSecretKeyModelList;
    }

    private GeneratedSecretKeyModel modelMapper(String owner, SecretKey secretKey) {
        GeneratedSecretKeyModel generatedSecretKeyModel = new GeneratedSecretKeyModel();
        generatedSecretKeyModel.setName(secretKey.getName());
        try {
            generatedSecretKeyModel.setSecret(decodeOTP(secretKey.getSecretKey()));
        } catch (InvalidKeyException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Not able to decode secret of " + secretKey.getName());
        }
        generatedSecretKeyModel.setId(secretKey.getId());
        generatedSecretKeyModel.setEmail(secretKey.getEmail());
        generatedSecretKeyModel.setUrl(secretKey.getUrl());
        generatedSecretKeyModel.setName(secretKey.getName());
        generatedSecretKeyModel.setPassword(secretKey.getPassword());
        if (superAdmins.contains(owner)) generatedSecretKeyModel.setIsOwner(true);
        else generatedSecretKeyModel.setIsOwner(owner.equalsIgnoreCase(secretKey.getOwner()));
        return generatedSecretKeyModel;
    }

    public Optional<GeneratedSecretKeyModel> getSecretKeyById(Integer id, String owner) {
        Optional<SecretKey> secretKeyOptional = secretKeyRepository.findById(id);
        if (secretKeyOptional.isPresent()) {
            SecretKey secretKey = secretKeyOptional.get();
            return Optional.of(modelMapper(owner, secretKey));
        }
        return Optional.<GeneratedSecretKeyModel>empty();
    }

    public void updateTOTP(Integer id, SecretKeyModel secretKeyModel, String owner) {

        SecretKey secretKey = recordBelongsToOwner(id, owner);

        secretKey.setName(secretKeyModel.getName());
        secretKey.setSecretKey(secretKey.getSecretKey());
        secretKey.setUrl(secretKeyModel.getUrl());
        secretKey.setEmail(secretKeyModel.getEmail());
        secretKey.setPassword(secretKeyModel.getPassword());
        secretKeyRepository.save(secretKey);
    }

    public void deleteTOTP(Integer id, String owner) {
        SecretKey secretKey = recordBelongsToOwner(id, owner);
        secretKeyRepository.delete(secretKey);
    }

    private String decodeOTP(String encodedKey) throws InvalidKeyException {


        // Key length should match the length of the HMAC output (160 bits for SHA-1, 256 bits
        // for SHA-256, and 512 bits for SHA-512). Note that while Mac#getMacLength() returns a
        // length in _bytes,_ KeyGenerator#init(int) takes a key length in _bits._
        Instant now = Instant.now();
        byte[] decodedKey = base32.decode(encodedKey);
        Key key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        String OTP = totp.generateOneTimePasswordString(key, now);
        logger.info("OTP {} for secretKey {} ", OTP, encodedKey);
        return OTP;
    }

    private SecretKey recordBelongsToOwner(Integer id, String owner) {
        Optional<SecretKey> secretKeyDAOOptional = secretKeyRepository.findById(id);
        if (secretKeyDAOOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id + " not found");
        }
        SecretKey secretKey = secretKeyDAOOptional.get();
        if (!secretKey.getOwner().equalsIgnoreCase(owner) && !superAdmins.contains(owner.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user " + owner + " not allowed to update " + secretKey.getName());
        }

        return secretKey;
    }

}
