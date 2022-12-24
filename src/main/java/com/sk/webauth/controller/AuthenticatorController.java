package com.sk.webauth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class AuthenticatorController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticatorController.class);

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/authenticate")
    public void Authenticate(@RequestHeader("owner-email") String owner) {
        logger.info("User {} successfully authenticated", owner);
    }
}
