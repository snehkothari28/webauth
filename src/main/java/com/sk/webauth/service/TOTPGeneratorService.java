package com.sk.webauth.service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.sk.webauth.dao.DelegationTable;
import com.sk.webauth.dao.SecretKey;
import com.sk.webauth.model.DelegationTableModel;
import com.sk.webauth.model.GeneratedSecretKeyModel;
import com.sk.webauth.repository.DelegationTableRepository;
import com.sk.webauth.repository.SecretKeyRepository;
import com.sk.webauth.util.DelegationModelConverter;
import jakarta.annotation.PostConstruct;
import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

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

    @Autowired
    private DelegationTableRepository delegationTableRepository;


    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        keyGenerator = KeyGenerator.getInstance(totp.getAlgorithm());
        int macLengthInBytes = Mac.getInstance(totp.getAlgorithm()).getMacLength();
        keyGenerator.init(macLengthInBytes * 8);
    }


    public List<GeneratedSecretKeyModel> getOTPAll(String owner, String requestId) {

        List<SecretKey> secretKeyDAOList = new ArrayList<>();
        if (superAdmins.contains(owner)) {
            secretKeyRepository.findAll().forEach(secretKeyDAOList::add);
        } else {
            secretKeyDAOList.addAll(secretKeyRepository.findByOwner(owner));

            List<DelegationTable> delegationTableList = delegationTableRepository.findByEmail(owner);
            secretKeyDAOList.addAll(delegationTableList.stream().map(DelegationTable::getSecretKey).toList());
        }
        List<GeneratedSecretKeyModel> generatedSecretKeyModelList = new ArrayList<>();
        for (SecretKey secretKey : secretKeyDAOList) {
            if (secretKey.getDeleted() == null || !secretKey.getDeleted())
                generatedSecretKeyModelList.add(modelMapper(owner, secretKey, true));
        }
        logger.info("Owner: {} requested all TOTP for requestId: {}", owner, requestId);
        return generatedSecretKeyModelList;
    }

    private GeneratedSecretKeyModel modelMapper(String owner, SecretKey secretKey, Boolean neglectDelegationTable) {
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
        generatedSecretKeyModel.setType(secretKey.getType());
        generatedSecretKeyModel.setDeleted(secretKey.getDeleted());
        generatedSecretKeyModel.setDeletedBy(secretKey.getDeletedBy());
        generatedSecretKeyModel.setDeletedAt(secretKey.getDeletedAt());

        List<DelegationTableModel> delegatedUserModel = DelegationModelConverter.getDelegatedUserModel(secretKey.getDelegationTableSet());
        if (!neglectDelegationTable) {
            generatedSecretKeyModel.addDelegationTable(delegatedUserModel);
        }

        if (superAdmins.contains(owner) || owner.equalsIgnoreCase(secretKey.getOwner())) {
            generatedSecretKeyModel.setOwner(true);
            generatedSecretKeyModel.setWriteUser(true);
            return generatedSecretKeyModel;
        }

        generatedSecretKeyModel.setWriteUser(delegatedUserModel.parallelStream().filter(e -> e.getEmail().equalsIgnoreCase(owner)).anyMatch(DelegationTableModel::getIsWriteUser));

        return generatedSecretKeyModel;
    }

    public Optional<GeneratedSecretKeyModel> getSecretKeyById(Integer id, String owner, String requestId) {
        Optional<SecretKey> secretKeyOptional = secretKeyRepository.findById(id);
        logger.info("Owner: {} requested TOTP of id {} for requestId: {}", owner, id, requestId);
        if (secretKeyOptional.isPresent()) {
            SecretKey secretKey = secretKeyOptional.get();
            GeneratedSecretKeyModel generatedSecretKeyModel = modelMapper(owner, secretKey, false);
            if (generatedSecretKeyModel.isWriteUser() || generatedSecretKeyModel.isOwner() || superAdmins.contains(owner.toLowerCase())) {
                return Optional.of(generatedSecretKeyModel);
            }
            logger.error("email {} is forbidden to access id: {} for request id {}", owner, id, requestId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, id + " forbidden");
        }
        return Optional.empty();
    }


    public void deleteTOTP(Integer id, String owner, String requestId) {
        SecretKey secretKey = recordBelongsToOwner(id, owner, requestId);
        logger.info("owner {} deleted id {} for request id {}", owner, id, requestId);
        secretKey.setDeleted(true);
        secretKey.setDeletedBy(owner);
        secretKey.setDeletedAt(LocalDateTime.now());
        secretKeyRepository.save(secretKey);
    }

    public List<GeneratedSecretKeyModel> getDeletedRecords(String owner, String requestId)
    {
        if(superAdmins.contains(owner))
        {
            List<SecretKey> deletedSecretKeys = secretKeyRepository.findByDeleted(true);
            List<GeneratedSecretKeyModel> generatedSecretKeyModelList = new ArrayList<>();
            for(SecretKey secretKey : deletedSecretKeys)
            {
                GeneratedSecretKeyModel generatedSecretKeyModel = modelMapper(owner, secretKey, false);
                generatedSecretKeyModel.setDeletedBy(secretKey.getDeletedBy());
                generatedSecretKeyModel.setDeletedAt(secretKey.getDeletedAt());
                generatedSecretKeyModelList.add(generatedSecretKeyModel);
            }
            logger.info("Owner {} requested all deleted TOTP for request id {}",owner,requestId);
            return generatedSecretKeyModelList;
        }
        else {
            logger.error("User {} not authorized to access deleted records for request id {}", owner, requestId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User not found");
        }
    }

    public void restoreDeletedRecords(Integer id, String owner, String requestId) {
        if(superAdmins.contains(owner)) {
            Optional<SecretKey> key = secretKeyRepository.findById(id);
            if (key.isPresent()) {
                SecretKey secretKey = key.get();
                secretKey.setDeleted(false);
                secretKey.setDeletedBy(null);
                secretKey.setDeletedAt(null);
                secretKeyRepository.save(secretKey);

                logger.info("Owner {} restored TOTP with id {} for request id {}", owner, id, requestId);
            } else {
                logger.error("Secret key with id {} not found for request id {}", id, requestId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Secret key not found");
            }
        } else {
            logger.error("User {} not authorized to restore deleted records for request id {}", owner, requestId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User not found");
        }
    }

    public Set<String> getTypes() {
        List<String> types = secretKeyRepository.findAllType();
        Set<String> uniqueTypes = new HashSet<>();
        types.forEach((type) -> {
            if (StringUtils.hasLength(type)) {
                boolean exists = false;
                for (String setType : uniqueTypes) {
                    if (setType.equalsIgnoreCase(type)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) uniqueTypes.add(type);
            }
        });
        return uniqueTypes;
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

    private SecretKey recordBelongsToOwner(Integer id, String owner, String requestId) {
        Optional<SecretKey> secretKeyDAOOptional = secretKeyRepository.findById(id);
        if (secretKeyDAOOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id + " not found");
        }
        SecretKey secretKey = secretKeyDAOOptional.get();
        if (!secretKey.getOwner().equalsIgnoreCase(owner) && !superAdmins.contains(owner.toLowerCase())) {
            logger.error("owner {} not allowed to delete id {} for request id {}", owner, id, requestId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user " + owner + " not allowed to delete " + secretKey.getName());
        }

        return secretKey;
    }

}
