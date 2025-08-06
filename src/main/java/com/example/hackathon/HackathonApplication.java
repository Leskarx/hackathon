package com.example.hackathon;

import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.hackathon.service.DuplicateScannerService;

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

        System.out.println("✅ Scan completed.");
    }

}
