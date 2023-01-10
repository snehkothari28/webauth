package com.sk.webauth.service;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.sk.webauth.dao.DelegationTable;
import com.sk.webauth.dao.SecretKey;
import com.sk.webauth.repository.DelegationTableRepository;
import com.sk.webauth.repository.SecretKeyRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class BackupCsvGenerator {
    private final Logger log = LoggerFactory.getLogger(BackupCsvGenerator.class);
    //    private final String[] HEADERS = Arrays.stream(GeneratedSecretKeyModel.class.getDeclaredFields()).map(Field::getName).toArray(String[]::new);
    @Value("${web.auth.backup.path}")
    private String path;
    @Autowired
    private SecretKeyRepository secretKeyRepository;

    @Autowired
    private DelegationTableRepository delegationTableRepository;

    @PostConstruct
    private void postConstruct() {
        if (!path.endsWith("/")) path = path + "/";
    }

    private String createCSV(List<SecretKey> generatedSecretKeyModelList) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {


        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new java.util.Date());

        try (Writer writer = new FileWriter(path + timestamp + "_SecretKey.csv")) {

            StatefulBeanToCsv<SecretKey> sbc = new StatefulBeanToCsvBuilder<SecretKey>(writer)
                    .withEscapechar('\\')
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .build();

            sbc.write(generatedSecretKeyModelList);
        } catch (IOException e) {
            log.error("Not able to write to file {}", path + timestamp + ".csv");
            log.error(Arrays.toString(e.getStackTrace()));
            throw e;
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            log.error("Not able to write to csv data");
            log.error(Arrays.toString(e.getStackTrace()));
            throw e;
        }
        return timestamp;
    }

    @Transactional
    public String startSecretKeyBackup() {
        String timestamp = "";
        try {
            List<SecretKey> secretKeyList = new ArrayList<>();
            secretKeyRepository.findAll().forEach(secretKeyList::add);

            timestamp = createCSV(secretKeyList);
        } catch (Exception e) {
            log.error("Unable to backup secret key with error");
            log.error(Arrays.toString(e.getStackTrace()));
        }
        return timestamp;
    }

    @Transactional
    public String startDelegationTableBackup() {
        String timestamp = "";
        try {
            List<DelegationTable> delegationTableList = new ArrayList<>();
            delegationTableRepository.findAll().forEach(delegationTableList::add);
            timestamp = createDelegationCSV(delegationTableList);
        } catch (Exception e) {
            log.error("Unable to backup secret key with error");
            log.error(Arrays.toString(e.getStackTrace()));
        }
        return timestamp;
    }

    private String createDelegationCSV(List<DelegationTable> delegationTableLists) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {


        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new java.util.Date());

        try (Writer writer = new FileWriter(path + timestamp + "_DelegationTable.csv")) {

            StatefulBeanToCsv<DelegationTable> sbc = new StatefulBeanToCsvBuilder<DelegationTable>(writer)
                    .withEscapechar('\\')
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .build();

            sbc.write(delegationTableLists);
        } catch (IOException e) {
            log.error("Not able to write to file {}", path + timestamp + ".csv");
            log.error(Arrays.toString(e.getStackTrace()));
            throw e;
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            log.error("Not able to write to csv data");
            log.error(Arrays.toString(e.getStackTrace()));
            throw e;
        }
        return timestamp;
    }
}
