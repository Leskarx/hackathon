package com.example.hackathon.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

public class DuplicateScannerService {

    private final Map<String, List<File>> hashMap = new HashMap<>();

    public void scanDirectory(String dirPath, boolean recursive) {
        File directory = new File(dirPath);

        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("❌ Invalid directory path.");
            return;
        }

        Collection<File> files = FileUtils.listFiles(directory, null, recursive);

        System.out.println("🔍 Scanning " + files.size() + " files...");

        for (File file : files) {
            try {
                String hash = DigestUtils.sha256Hex(FileUtils.readFileToByteArray(file));
                hashMap.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);
            } catch (IOException e) {
                System.out.println("⚠️ Failed to read file: " + file.getAbsolutePath());
            }
        }

        displayDuplicatesAndPromptDeletion();
    }

    private void displayDuplicatesAndPromptDeletion() {
        System.out.println("\n📑 Duplicate Files Found:");
    
        Scanner scanner = new Scanner(System.in);
        int group = 1;
    
        for (Map.Entry<String, List<File>> entry : hashMap.entrySet()) {
            List<File> duplicates = entry.getValue();
            if (duplicates.size() > 1) {
                System.out.println("\n🧾 Group " + group + ":");
    
                for (int i = 0; i < duplicates.size(); i++) {
                    System.out.println(" [" + i + "] " + duplicates.get(i).getAbsolutePath());
                }
    
                System.out.print("❓ Enter the indices of files you want to delete (comma separated), or press Enter to skip: ");
                String input = scanner.nextLine();
    
                if (!input.trim().isEmpty()) {
                    String[] indices = input.split(",");
                    for (String indexStr : indices) {
                        try {
                            int index = Integer.parseInt(indexStr.trim());
                            File fileToDelete = duplicates.get(index);
                            if (fileToDelete.delete()) {
                                System.out.println("✅ Deleted: " + fileToDelete.getAbsolutePath());
                            } else {
                                System.out.println("❌ Failed to delete: " + fileToDelete.getAbsolutePath());
                            }
                        } catch (NumberFormatException | IndexOutOfBoundsException e) {
                            System.out.println("⚠️ Invalid index: " + indexStr.trim());
                        }
                    }
                } else {
                    System.out.println("➡️ Skipping deletion for this group.");
                }
    
                group++;
            }
        }
    }
    
}
