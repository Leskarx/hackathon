package com.example.hackathon.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

@Service
public class Logwritter {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE_NAME = "appcleaner-log.txt";

    static {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    // Call this once before writing logs
    public static void initLogFile() {
        File logFile = new File(LOG_DIR, LOG_FILE_NAME);
        if (logFile.exists()) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File archivedFile = new File(LOG_DIR, "appcleaner-log-" + timestamp + ".txt");

            boolean renamed = logFile.renameTo(archivedFile);
            if (renamed) {
                System.out.println("🗂️ Archived previous log file as: " + archivedFile.getName());
            } else {
                System.out.println("⚠️ Failed to archive existing log file.");
            }
        }
    }

    public static void write(String message) {
        try (FileWriter writer = new FileWriter(new File(LOG_DIR, LOG_FILE_NAME), true)) {
            writer.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " - " + message + "\n");
        } catch (IOException e) {
            System.out.println("❌ Failed to write to log file: " + e.getMessage());
        }
    }

    public static String getLogFilePath() {
        return new File(LOG_DIR, LOG_FILE_NAME).getAbsolutePath();
    }

    public static File getLogFile() {
        return new File(LOG_DIR, LOG_FILE_NAME);
    }
}
