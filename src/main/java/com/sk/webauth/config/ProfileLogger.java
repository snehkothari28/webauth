package com.sk.webauth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class ProfileLogger {

    @Autowired
    private Environment env;

    @PostConstruct
    public void logActiveProfiles() {
        String[] profiles = env.getActiveProfiles();
        System.out.println("=== Active Spring Profiles ===");
        if (profiles.length == 0) {
            System.out.println("No active profiles set. Default profile will be used.");
        } else {
            for (String profile : profiles) {
                System.out.println("Active Profile: " + profile);
            }
        }
        System.out.println("==============================");
    }
}
