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
import org.springframework.stereotype.Service;

import com.example.hackathon.helper.GetfileExtension;

@Service
public class DuplicateScannerService {

    private final Map<String, List<File>> hashMap = new HashMap<>();
    private final LoggerService logger = new LoggerService();
    private final GetfileExtension getFileExtension=new GetfileExtension();

    private static final Map<String, String> CATEGORY_RULES = Map.of(
        "exe", "Executables",
        "pdf", "Documents",
        "docx", "Documents",
        "txt", "TextFiles",
        "png", "Images",
        "jpg", "Images",
        "jpeg", "Images",
        "zip", "Archives",
        "rar", "Archives"
    );


    public void scanDirectory(String dirPath, boolean recursive) {
        File directory = new File(dirPath);
    
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("❌ Invalid directory path.");
            return;
        }
    
        logger.log("Scanning directory: " + dirPath + " (recursive: " + recursive + ")");
        Collection<File> files = FileUtils.listFiles(directory, null, recursive);
        System.out.println("🔍 Scanning " + files.size() + " files...");
    
        List<File> allFiles = new ArrayList<>(); // to store non-deleted files
    
        for (File file : files) {
            try {
                String hash = DigestUtils.sha256Hex(FileUtils.readFileToByteArray(file));
                hashMap.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);
            } catch (IOException e) {
                logger.log("Failed to read file: " + file.getAbsolutePath());
            }
        }
    
        displayDuplicatesAndPromptDeletion();  // May delete some files
    
        // Collect remaining non-duplicate files
        for (List<File> list : hashMap.values()) {
            if (list.size() == 1) {
                allFiles.addAll(list);
            } else {
                allFiles.add(list.get(0));  // keep one copy
            }
        }
    
        // Categorize
        categorizeFiles(allFiles, dirPath);
    }
    
    private void categorizeFiles(Collection<File> files, String baseDir) {
        logger.log("📂 Starting file categorization...");
    
        for (File file : files) {
            String ext = getFileExtension.getFileExtension(file);
            String category = CATEGORY_RULES.getOrDefault(ext, "Others");
    
            File categoryDir = new File(baseDir, category);
            if (!categoryDir.exists()) {
                categoryDir.mkdirs();
                logger.log("📁 Created category directory: " + categoryDir.getAbsolutePath());
            }
    
            File targetFile = new File(categoryDir, file.getName());
    
            try {
                FileUtils.moveFile(file, targetFile);
                logger.log("📦 Moved file to category: " + targetFile.getAbsolutePath());
                System.out.println("➡️ Moved: " + file.getName() + " → " + category + "/");
            } catch (IOException e) {
                logger.log("❌ Failed to move file: " + file.getAbsolutePath());
                System.out.println("⚠️ Failed to move: " + file.getName());
            }
        }
    
        logger.log("✅ Categorization complete.");
    }
    


    private void displayDuplicatesAndPromptDeletion() {
        System.out.println("\n📑 Duplicate Files Found:");
    
        Scanner scanner = new Scanner(System.in);
        int group = 1;
        int totalDuplicateGroups = 0;
    
        for (Map.Entry<String, List<File>> entry : hashMap.entrySet()) {
            List<File> duplicates = entry.getValue();
            if (duplicates.size() > 1) {
                totalDuplicateGroups++;
    
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
                                logger.log("Deleted file: " + fileToDelete.getAbsolutePath());
                                System.out.println("✅ Deleted: " + fileToDelete.getAbsolutePath());
                            } else {
                                logger.log("Failed to delete file: " + fileToDelete.getAbsolutePath());
                                System.out.println("❌ Failed to delete: " + fileToDelete.getAbsolutePath());
                            }
                        } catch (NumberFormatException | IndexOutOfBoundsException e) {
                            logger.log("Invalid deletion index entered: " + indexStr.trim());
                            System.out.println("⚠️ Invalid index: " + indexStr.trim());
                        }
                    }
                } else {
                    System.out.println("➡️ Skipping deletion for this group.");
                }
    
                group++;
            }
        }
    
        if (totalDuplicateGroups == 0) {
            System.out.println("🎉 No duplicate files found.");
            logger.log("No duplicate files found.");
        } else {
            logger.log("Total duplicate groups found: " + totalDuplicateGroups);
        }
    }
    
}
