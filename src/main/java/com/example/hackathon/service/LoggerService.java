package com.example.hackathon.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class LoggerService {
    private final String logFilePath;

    public LoggerService() {
        this.logFilePath = "appcleaner-log.txt"; // or System.getProperty("user.home") + "/appcleaner-log.txt"
    }

    public void log(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
            writer.write("[" + LocalDateTime.now() + "] " + message + "\n");
        } catch (IOException e) {
            System.out.println("⚠️ Failed to write to log file: " + e.getMessage());
        }
    }
}
