package com.sk.webauth.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.apache.http.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.text.ParseException;

@Service
public class AuthenticationService {

    private final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    private final String tenantId;
    private final String clientId;

    public AuthenticationService(@Value("${azure.tenant.id}") String tenantId, @Value("${azure.client.id}") String clientId) throws MalformedURLException {
        this.tenantId = tenantId;
        this.clientId = clientId;

        String jwksUri = "https://login.microsoftonline.com/" + tenantId + "/discovery/v2.0/keys";
        JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(jwksUri));

        jwtProcessor = new DefaultJWTProcessor<>();
        JWSVerificationKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
    }

    public String verifyToken(String idToken, String contextPath, String requestId) throws AuthenticationException {
        try {
            if (idToken == null || idToken.isEmpty()) {
                throw new AuthenticationException("Missing token");
            }

            SignedJWT signedJWT = SignedJWT.parse(idToken);
            SecurityContext ctx = null;
            var claimsSet = jwtProcessor.process(signedJWT, ctx);

            if (!claimsSet.getAudience().contains(clientId)) {
                throw new AuthenticationException("Invalid audience");
            }

            Instant exp = claimsSet.getExpirationTime().toInstant();
            Instant now = Instant.now();
            if (exp.isBefore(now)) {
                log.warn("Token expired for requestId: {}", requestId);
                throw new AuthenticationException("Token expired");
            }

            String email = claimsSet.getStringClaim("preferred_username");
            log.info("User {} authenticated at {} for requestId {}", email, now, requestId);

            return email;

        } catch (ParseException | JOSEException | BadJOSEException e) {
            log.error("JWT verification failed: {}", e.getMessage());
            throw new AuthenticationException("Invalid token");
        }
    }

}
