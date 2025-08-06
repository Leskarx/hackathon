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
    
        // Initialize and archive logs
        Logwritter.initLogFile();
    
        // Directory input
        System.out.print("📂 Enter the directory path to scan: ");
        String directory = scanner.nextLine();
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("❌ Invalid directory path.");
            return;
        }
    
        // Recursive option
        System.out.print("🔁 Scan subdirectories? (y/n): ");
        boolean recursive = scanner.nextLine().trim().equalsIgnoreCase("y");
    
        // 🔘 Show user menu
        System.out.println("\nChoose an operation:");
        System.out.println("1. 🗑️ Delete Duplicate Files");
        System.out.println("2. 🗂️ Categorize Files by Type");
        System.out.println("3. 🔄 Both (Delete Duplicates & Categorize)");
        System.out.println("4. ❌ Exit");
    
        System.out.print("➡️ Enter choice (1-4): ");
        String choice = scanner.nextLine().trim();
    
        switch (choice) {
            case "1":
                scannerService.deleteDuplicatesOnly(directory, recursive);
                break;
            case "2":
                scannerService.categorizeOnly(directory, recursive);
                break;
            case "3":
                scannerService.scanDirectory(directory, recursive); // Original full flow
                break;
            case "4":
                System.out.println("👋 Exiting application.");
                return;
            default:
                System.out.println("⚠️ Invalid choice. Exiting.");
                return;
        }
    
        // Ask for email
        System.out.print("📧 Do you want to email the log file? (y/n): ");
        String emailOption = scanner.nextLine();
    
        if (emailOption.equalsIgnoreCase("y")) {
            System.out.print("✉️ Enter the recipient's email address: ");
            String toEmail = scanner.nextLine();
    
            File logFile = Logwritter.getLogFile();
    
            if (logFile.exists()) {
                emailService.sendEmailWithAttachment(toEmail, logFile);
            } else {
                System.out.println("⚠️ Log file not found.");
            }
        }
    
        System.out.println("✅ Operation completed.");
        scanner.close();
    }
    
}
