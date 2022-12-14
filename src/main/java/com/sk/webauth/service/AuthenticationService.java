package com.sk.webauth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.apache.http.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Service
public class AuthenticationService {
    private static final GsonFactory gsonFactory = new GsonFactory();

    private final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private final GoogleIdTokenVerifier verifier;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public AuthenticationService(@Value("${gsi.client.id}") String CLIENT_ID) {
        if (!StringUtils.hasLength(CLIENT_ID)) throw new RuntimeException("google client id empty.");

        verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), gsonFactory)
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();
    }

    public String verifyToken(String idTokenString, String contextPath,
                              String requestId) throws AuthenticationException {
        GoogleIdToken idToken = null;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (GeneralSecurityException | IOException e) {
            log.warn(e.getLocalizedMessage());
        }
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            // Print user identifier
            String userId = payload.getSubject();
            // Get profile information from payload
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            LocalDateTime now = LocalDateTime.now();

            long epoch = System.currentTimeMillis() / 1000;

            if (payload.getExpirationTimeSeconds() < epoch) {
                log.warn("token expired for name: {} email: {} userId: {} accessing at: {} at {} for requestId: {}", name, email, userId, contextPath, dtf.format(now), requestId);

            }
            log.info("name: {} email: {} userId: {} accessed: {} at {} for requestId: {}", name, email, userId, contextPath, dtf.format(now), requestId);
            return email;

        } else {
            log.warn("Invalid ID token: {} for requestId: {}", idTokenString, requestId);
            throw new AuthenticationException("Invalid Token Id");
        }
    }

}
