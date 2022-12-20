package com.sk.webauth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class AuthenticatorController {

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/authenticate")
    public void Authenticate() {

    }
}
