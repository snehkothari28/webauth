package com.sk.webauth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController()
public class AuthenticatorController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticatorController.class);

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/authenticate")
    public void Authenticate(@RequestHeader("owner-email") String owner, @RequestHeader("requestId") String requestId) {
        logger.info("User {} successfully authenticated for requestId: {}", owner, requestId);
    }
}
