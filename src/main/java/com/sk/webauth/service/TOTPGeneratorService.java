package com.sk.webauth.service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.sk.webauth.dao.SecretKeyDAO;
import com.sk.webauth.model.GeneratedSecretKeyModel;
import com.sk.webauth.model.SecretKeyModel;
import com.sk.webauth.repository.SecretKeyRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private SecretKeyRepository secretKeyRepository;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        keyGenerator = KeyGenerator.getInstance(totp.getAlgorithm());
        int macLengthInBytes = Mac.getInstance(totp.getAlgorithm()).getMacLength();
        keyGenerator.init(macLengthInBytes * 8);
    }


    public List<GeneratedSecretKeyModel> getOTPAll() throws InvalidKeyException {
        Iterable<SecretKeyDAO> secretKeyDAOList = secretKeyRepository.findAll();
        List<GeneratedSecretKeyModel> generatedSecretKeyModelList = new ArrayList<>();
        for (SecretKeyDAO secretKeyDAO : secretKeyDAOList) {
            GeneratedSecretKeyModel generatedSecretKeyModel = new GeneratedSecretKeyModel();
            generatedSecretKeyModel.setName(secretKeyDAO.getName());
            generatedSecretKeyModel.setSecret(decodeOTP(secretKeyDAO.getSecretKey()));
            generatedSecretKeyModel.setId(secretKeyDAO.getId());
            generatedSecretKeyModelList.add(generatedSecretKeyModel);
        }
        return generatedSecretKeyModelList;
    }

    public Optional<SecretKeyModel> getSecretKeyById(Integer id) throws InvalidKeyException {
        Optional<SecretKeyDAO> secretKeyDAO = secretKeyRepository.findById(id);
        if (secretKeyDAO.isPresent()) {
            SecretKeyModel secretKeyModel = new SecretKeyModel();
            secretKeyModel.setId(id);
            secretKeyModel.setName(secretKeyDAO.get().getName());
            secretKeyModel.setSecretKey(secretKeyDAO.get().getSecretKey());
            return Optional.of(secretKeyModel);
        }

        return Optional.<SecretKeyModel>empty();

    }

    public void updateTOTP(Integer id, SecretKeyModel secretKeyModel) throws InvalidKeyException {
        Optional<SecretKeyDAO> secretKeyDAOOptional = secretKeyRepository.findById(id);
        if (secretKeyDAOOptional.isEmpty()) {
            throw new NullPointerException("invalid id " + id);
        }
        SecretKeyDAO secretKeyDAO = secretKeyDAOOptional.get();
        secretKeyDAO.setName(secretKeyModel.getName());
        secretKeyDAO.setSecretKey(secretKeyModel.getSecretKey());
        secretKeyRepository.save(secretKeyDAO);
    }

    public void deleteTOTP(Integer id) throws InvalidKeyException {
        secretKeyRepository.deleteById(id);
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
//    private Key encodeKey(String secretKey) {
//        byte[] decodedKey = base32.decode(secretKey);
//        Key key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
////        String encodedString = base32.encodeToString(key.getEncoded());
//
////        logger.info("encodedString is " + encodedString + " for secretKey key " + secretKey);
//        return key;
//    }

//    private void printCurrentTOTP(Key key) throws InvalidKeyException {
//
//
//        System.out.println("Current password: " + totp.generateOneTimePasswordString(key, now));
//    }
}
