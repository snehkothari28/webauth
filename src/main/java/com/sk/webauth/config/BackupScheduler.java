package com.sk.webauth.config;

import com.sk.webauth.model.GeneratedSecretKeyModel;
import com.sk.webauth.service.BackupCsvGenerator;
import com.sk.webauth.service.TOTPGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableScheduling
public class BackupScheduler {
    public static final String BACKUP_SCHEDULER = "BackupScheduler";
    private final Logger log = LoggerFactory.getLogger(BackupScheduler.class);
    @Autowired
    private TOTPGeneratorService totpGeneratorService;

    @Autowired
    private BackupCsvGenerator backupCsvGenerator;

    @Scheduled(cron = "${web.auth.backup.cron}")
    public void backupCronJob() {
        backup();
    }

    public void backup() {
        log.info("Starting backup service");
        try {
            List<GeneratedSecretKeyModel> generatedSecretKeyModelList = totpGeneratorService.getOTPAll(BACKUP_SCHEDULER, "backupService");
            String timestamp = backupCsvGenerator.createCSV(generatedSecretKeyModelList);

            log.info("Successfully backup at " + timestamp);

        } catch (Exception e) {
            log.error("Failed backing up with stacktrace:");
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }
}
