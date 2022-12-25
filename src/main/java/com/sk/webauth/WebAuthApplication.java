package com.sk.webauth;

import com.sk.webauth.config.BackupScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebAuthApplication implements CommandLineRunner {
    @Autowired
    private BackupScheduler backupScheduler;

    public static void main(String[] args) {
        SpringApplication.run(WebAuthApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        backupScheduler.backup();
    }
}
