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
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private final GoogleIdTokenVerifier verifier;

    public AuthenticationService(@Value("${gsi.client.id}") String CLIENT_ID) {
        if (!StringUtils.hasLength(CLIENT_ID)) throw new RuntimeException("google client id empty.");

        verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), gsonFactory)
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();
    }

    public void verifyToken(String idTokenString, String contextPath
    ) throws AuthenticationException {
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

            log.info("name: {} email: {} userId: {} accessed: {} at {}", name, email, userId, contextPath, dtf.format(now));
        } else {
            log.warn("Invalid ID token: " + idTokenString);
            throw new AuthenticationException("Invalid Token Id");
        }
    }

}
