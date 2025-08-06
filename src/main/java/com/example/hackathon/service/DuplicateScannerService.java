package com.example.hackathon.service;

import java.io.File;
import java.io.IOException;
import java.util.*;
// import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import com.example.hackathon.helper.GetfileExtension;
import com.example.hackathon.rules.CategorizationRule;
// import com.example.hackathon.service.Logwritter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DuplicateScannerService {

    private final Map<String, List<File>> hashMap = new HashMap<>();
    private final GetfileExtension getFileExtension = new GetfileExtension();
    private List<CategorizationRule> metadataRules = new ArrayList<>();

    public DuplicateScannerService() {
        loadCategorizationRules(); // ✅ Load rules from JSON file at startup
    }

    private void loadCategorizationRules() {
        try {
            File jsonFile = new File("metadata_rules.json"); // ✅ JSON must be in root dir or provide full path
            if (jsonFile.exists()) {
                ObjectMapper objectMapper = new ObjectMapper();
                metadataRules = objectMapper.readValue(jsonFile, new TypeReference<List<CategorizationRule>>() {});
                System.out.println("✅ Metadata categorization rules loaded: " + metadataRules.size());
            } else {
                System.out.println("⚠️ metadata_rules.json not found. Skipping metadata rules.");
            }
        } catch (IOException e) {
            System.out.println("❌ Error loading metadata rules: " + e.getMessage());
        }
    }

    public void scanDirectory(String path, boolean recursive) {
        hashMap.clear(); // Reset before every new scan

        Collection<File> files = FileUtils.listFiles(new File(path), null, recursive);
        for (File file : files) {
            try {
                String fileHash = DigestUtils.sha256Hex(FileUtils.readFileToByteArray(file));
                hashMap.computeIfAbsent(fileHash, k -> new ArrayList<>()).add(file);
            } catch (IOException e) {
                System.out.println("❌ Failed to read file: " + file.getAbsolutePath());
            }
        }

        deleteDuplicates();
        categorizeApplications(path);
    }

    public void deleteDuplicatesOnly(String path, boolean recursive) {
        hashMap.clear();

        Collection<File> files = FileUtils.listFiles(new File(path), null, recursive);
        for (File file : files) {
            try {
                String fileHash = DigestUtils.sha256Hex(FileUtils.readFileToByteArray(file));
                hashMap.computeIfAbsent(fileHash, k -> new ArrayList<>()).add(file);
            } catch (IOException e) {
                System.out.println("❌ Failed to read file: " + file.getAbsolutePath());
            }
        }

        deleteDuplicates();
    }

    public void categorizeOnly(String path, boolean recursive) {
        categorizeApplications(path);
    }

    private void deleteDuplicates() {
        Scanner scanner = new Scanner(System.in);
    
        int groupIndex = 1;
    
        for (List<File> duplicates : hashMap.values()) {
            if (duplicates.size() > 1) {
                System.out.println("\n🔁 Duplicate Group " + groupIndex++);
                for (int i = 0; i < duplicates.size(); i++) {
                    System.out.println("[" + i + "] " + duplicates.get(i).getAbsolutePath());
                }
    
                System.out.print("➡️ Enter index of file to KEEP (e.g., 0): ");
                String input = scanner.nextLine();
    
                int keepIndex;
                try {
                    keepIndex = Integer.parseInt(input);
                    if (keepIndex < 0 || keepIndex >= duplicates.size()) {
                        System.out.println("⚠️ Invalid index. Skipping this group.");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Invalid input. Skipping this group.");
                    continue;
                }
    
                for (int i = 0; i < duplicates.size(); i++) {
                    if (i != keepIndex) {
                        File file = duplicates.get(i);
                        if (file.delete()) {
                            System.out.println("🗑️ Deleted: " + file.getAbsolutePath());
                            Logwritter.write("🗑️ Deleted: " + file.getAbsolutePath());
                        } else {
                            System.out.println("❌ Failed to delete: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    
        System.out.println("\n✅ Duplicate deletion process completed.");
    }
    

    private void categorizeApplications(String basePath) {
        Collection<File> files = FileUtils.listFiles(new File(basePath), null, true);

        for (File file : files) {
            String extension = getFileExtension.getFileExtension(file);
            String category = getCategoryFromRules(file, extension);

            if (category != null && !category.isEmpty()) {
                File targetDir = new File(basePath + File.separator + category);
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }

                File dest = new File(targetDir, file.getName());
                try {
                    FileUtils.moveFile(file, dest);
                    System.out.println("➡️ Moved: " + file.getName() + " → " + category + "/");
                    Logwritter.write("➡️ Moved: " + file.getName() + " → " + category + "/");
                } catch (IOException e) {
                    System.out.println("❌ Failed to move: " + file.getAbsolutePath());
                }
            }
        }
    }

    private String getCategoryFromRules(File file, String extension) {
        // ✅ First try metadata rules
        for (CategorizationRule rule : metadataRules) {
            if (file.getName().toLowerCase().contains(rule.getMatch().toLowerCase())) {
                return rule.getCategory();
            }
        }

        // ✅ Then fall back to extension-based
        Map<String, String> CATEGORY_RULES = Map.of(
            "exe", "Executables",
            "pdf", "Documents",
            "docx", "Documents",
            "txt", "TextFiles",
            "png", "Images",
            "jpg", "Images",
            "jpeg", "Images",
            "mp4", "Videos",
            "mkv", "Videos"
        );

        return CATEGORY_RULES.getOrDefault(extension, "Others");
    }
}
