package com.example.hackathon;

import java.io.File;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.hackathon.service.DuplicateScannerService;
import com.example.hackathon.service.EmailService;
import com.example.hackathon.service.Logwritter;


@SpringBootApplication
public class HackathonApplication implements CommandLineRunner {

    @Autowired
    private DuplicateScannerService scannerService;

    @Autowired
    private EmailService emailService;

    public static void main(String[] args) {
        SpringApplication.run(HackathonApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("📦 Welcome to Duplicate App Cleaner");
        System.out.println("====================================");

        // 🧹 Initialize log file (delete existing or archive it)
        Logwritter.initLogFile();

        // 🗂 Directory input
        System.out.print("📂 Enter the directory path to scan: ");
        String directory = scanner.nextLine();
        System.out.println("📁 Scanning: " + directory);

        // 🔄 Recursive scan option
        System.out.print("🔁 Scan subdirectories? (y/n): ");
        boolean recursive = scanner.nextLine().trim().equalsIgnoreCase("y");

        // 🚀 Start scanning
        scannerService.scanDirectory(directory, recursive);

        // 📧 Ask if email should be sent
        System.out.print("📧 Do you want to email the log file? (y/n): ");
        String emailOption = scanner.nextLine();

        if (emailOption.equalsIgnoreCase("y")) {
            System.out.print("✉️ Enter the recipient's email address: ");
            String toEmail = scanner.nextLine();

            File logFile = new File("logs/appcleaner-log.txt");

            if (logFile.exists()) {
                emailService.sendEmailWithAttachment(toEmail, logFile);
            } else {
                System.out.println("⚠️ Log file not found.");
            }
        }

        System.out.println("✅ Scan completed.");
        
        // ✅ Close scanner to prevent memory/resource leaks
        scanner.close();
    }
}
