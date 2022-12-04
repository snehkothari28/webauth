package com.sk.webauth.service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import jakarta.annotation.PostConstruct;
import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Component
public class TOTPGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(TOTPGeneratorService.class);

    TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
    KeyGenerator keyGenerator;
    Base32 base32 = new Base32();

    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        keyGenerator = KeyGenerator.getInstance(totp.getAlgorithm());
        int macLengthInBytes = Mac.getInstance(totp.getAlgorithm()).getMacLength();
        keyGenerator.init(macLengthInBytes * 8);
    }


    private void decodeOTP(String encodedKey) throws InvalidKeyException {


        // Key length should match the length of the HMAC output (160 bits for SHA-1, 256 bits
        // for SHA-256, and 512 bits for SHA-512). Note that while Mac#getMacLength() returns a
        // length in _bytes,_ KeyGenerator#init(int) takes a key length in _bits._

        byte[] decodedKey = base32.decode(encodedKey);
        Key key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        System.out.println("Key is " + base32.encodeToString(key.getEncoded()));
    }

    private void printCurrentTOTP(Key key) throws InvalidKeyException {
        Instant now = Instant.now();

        System.out.println("Current password: " + totp.generateOneTimePasswordString(key, now));
    }
}
