package com.example.hackathon.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class LoggerService {

    private static final String LOG_DIR = "logs";
    private final String logFilePath;

    public LoggerService() {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        this.logFilePath = LOG_DIR + "/appcleaner-log.txt";
    }

    public void log(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
            writer.write("[" + LocalDateTime.now() + "] " + message + "\n");
        } catch (IOException e) {
            System.out.println("⚠️ Failed to write to log file: " + e.getMessage());
        }
    }

    public File getLogFile() {
        return new File(logFilePath);
    }
}
