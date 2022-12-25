package com.sk.webauth.service;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.sk.webauth.model.GeneratedSecretKeyModel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

@Service
public class BackupCsvGenerator {
    private final Logger log = LoggerFactory.getLogger(BackupCsvGenerator.class);
    //    private final String[] HEADERS = Arrays.stream(GeneratedSecretKeyModel.class.getDeclaredFields()).map(Field::getName).toArray(String[]::new);
    @Value("${web.auth.backup.path}")
    private String path;

    @PostConstruct
    private void postConstruct() {
        if (!path.endsWith("/")) path = path + "/";
    }


    public String createCSV(List<GeneratedSecretKeyModel> generatedSecretKeyModelList) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {


        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new java.util.Date());

        try (Writer writer = new FileWriter(path + timestamp + ".csv")) {

            StatefulBeanToCsv<GeneratedSecretKeyModel> sbc = new StatefulBeanToCsvBuilder<GeneratedSecretKeyModel>(writer)
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
}
