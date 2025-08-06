package com.example.hackathon;

import java.io.File;
import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.hackathon.service.DuplicateScannerService;
import com.example.hackathon.service.EmailService;

@SpringBootApplication
public class HackathonApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(HackathonApplication.class, args);
	}
	@Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("📦 Welcome to Duplicate App Cleaner");
        System.out.println("====================================");

        System.out.print("📂 Enter the directory path to scan: ");
        String directory = scanner.nextLine();
		System.out.println(directory);

        System.out.print("🔁 Scan subdirectories? (y/n): ");
        boolean recursive = scanner.nextLine().trim().equalsIgnoreCase("y");

        // Here you can call your scanning service
        DuplicateScannerService scannerService = new DuplicateScannerService();
        scannerService.scanDirectory(directory, recursive);
		
		System.out.print("📧 Do you want to email the log file? (y/n): ");
String emailOption = scanner.nextLine();

if (emailOption.equalsIgnoreCase("y")) {
    System.out.print("✉️ Enter the recipient's email address: ");
    String toEmail = scanner.nextLine();

    File logFile = new File("appcleaner-log.txt");
    if (logFile.exists()) {
        EmailService emailService = new EmailService();
        emailService.sendEmailWithAttachment(toEmail, logFile);
    } else {
        System.out.println("⚠️ Log file not found.");
    }
}


        System.out.println("✅ Scan completed.");
    }

}
